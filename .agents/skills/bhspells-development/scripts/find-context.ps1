[CmdletBinding()]
param(
    [Parameter(Mandatory = $true, Position = 0)]
    [ValidateNotNullOrEmpty()]
    [string]$Query,

    [ValidateSet('All', 'Spell', 'System', 'Docs', 'Assets')]
    [string]$Kind = 'All',

    [ValidateRange(1, 500)]
    [int]$MaxResults = 100
)

$ErrorActionPreference = 'Stop'

if (-not (Get-Command rg -ErrorAction SilentlyContinue)) {
    throw 'ripgrep (rg) is required to run the BHSpells context locator.'
}

$repoRoot = (& git -C $PSScriptRoot rev-parse --show-toplevel 2>$null | Select-Object -First 1)
if (-not $repoRoot) {
    $repoRoot = (Resolve-Path (Join-Path $PSScriptRoot '..\..\..\..')).Path
}
$repoRoot = [System.IO.Path]::GetFullPath([string]$repoRoot)

$javaRoot = 'src/main/java/net/offkung/bhspells'
$resourceRoot = 'src/main/resources'

$rootsByKind = @{
    All = @(
        'AGENTS.md', 'build.gradle', 'gradle.properties', 'docs',
        $javaRoot, $resourceRoot
    )
    Spell = @(
        'AGENTS.md', 'docs/spells', 'docs/spell_mechanics.md', 'docs/all_spells.md',
        "$javaRoot/spells", "$javaRoot/entity/spells", "$javaRoot/effect",
        "$javaRoot/event", "$javaRoot/service", "$javaRoot/network", "$javaRoot/pressure",
        "$javaRoot/client", "$javaRoot/compat", "$javaRoot/config/SpellConfig.java",
        "$javaRoot/registry/BHSpellRegistry.java", "$javaRoot/registry/EntityRegistry.java",
        "$javaRoot/registry/MobEffectsRegistry.java", "$javaRoot/registry/ParticleRegistry.java",
        "$javaRoot/registry/BHSoundRegistry.java", "$resourceRoot/assets/bhspells",
        "$resourceRoot/data/bhspells"
    )
    System = @(
        'AGENTS.md', 'build.gradle', 'gradle.properties', 'docs',
        $javaRoot, $resourceRoot
    )
    Docs = @('AGENTS.md', 'docs')
    Assets = @("$resourceRoot/assets/bhspells", "$resourceRoot/data/bhspells", "$resourceRoot/META-INF/mods.toml", "$resourceRoot/bhspells.mixins.json")
}

$trimmed = $Query.Trim()
$withoutNamespace = if ($trimmed.Contains(':')) { $trimmed.Split(':')[-1] } else { $trimmed }
$tokens = @($withoutNamespace -split '[^A-Za-z0-9]+' | Where-Object { $_ })
$snake = (($tokens | ForEach-Object { $_.ToLowerInvariant() }) -join '_')
$compact = ($tokens -join '')
$pascal = (($tokens | ForEach-Object {
    if ($_.Length -eq 1) { $_.ToUpperInvariant() }
    else { $_.Substring(0, 1).ToUpperInvariant() + $_.Substring(1) }
}) -join '')

$variantSet = [System.Collections.Generic.HashSet[string]]::new([System.StringComparer]::OrdinalIgnoreCase)
foreach ($variant in @($trimmed, $withoutNamespace, $snake, $compact, $pascal)) {
    if (-not [string]::IsNullOrWhiteSpace($variant)) {
        [void]$variantSet.Add($variant)
    }
}
$variants = @($variantSet)
$normalizedQuery = ($withoutNamespace -replace '[^A-Za-z0-9]', '').ToLowerInvariant()

Push-Location $repoRoot
try {
    $searchRoots = @($rootsByKind[$Kind] | Where-Object { Test-Path -LiteralPath $_ })
    if ($searchRoots.Count -eq 0) {
        throw "No search roots exist for kind '$Kind' under $repoRoot."
    }

    $allFiles = @(& rg --files -- @searchRoots 2>$null)
    $pathMatches = @($allFiles | Where-Object {
        (($_ -replace '[^A-Za-z0-9]', '').ToLowerInvariant()).Contains($normalizedQuery)
    })

    $rgArguments = @('-l', '-i', '-F')
    foreach ($variant in $variants) {
        $rgArguments += @('-e', $variant)
    }
    $rgArguments += '--'
    $rgArguments += $searchRoots
    $contentMatches = @(& rg @rgArguments 2>$null)
    if ($LASTEXITCODE -gt 1) {
        throw "ripgrep failed with exit code $LASTEXITCODE."
    }

    $ranked = @{}
    foreach ($path in $contentMatches) {
        $ranked[$path] = 1
    }
    foreach ($path in $pathMatches) {
        $ranked[$path] = 0
    }

    function Get-ContextTag([string]$Path) {
        $normalized = $Path.Replace('/', '\')
        switch -Regex ($normalized) {
            '^docs\\spells\\' { return 'DOC-SPELL' }
            '^docs\\' { return 'DOC' }
            '^src\\main\\resources\\' { return 'ASSET' }
            '\\client\\' { return 'CLIENT' }
            '\\network\\|\\pressure\\network\\' { return 'NETWORK' }
            '\\registry\\' { return 'REGISTRY' }
            '\\config\\' { return 'CONFIG' }
            '\\mixin\\' { return 'MIXIN' }
            '\\pressure\\' { return 'PRESSURE' }
            '\\entity\\' { return 'ENTITY' }
            '\\effect\\' { return 'EFFECT' }
            '\\event\\|\\service\\' { return 'LIFECYCLE' }
            '\\compat\\' { return 'COMPAT' }
            '\\spells\\' { return 'SPELL' }
            '\.java$' { return 'JAVA' }
            default { return 'PROJECT' }
        }
    }

    $results = @($ranked.GetEnumerator() | ForEach-Object {
        [PSCustomObject]@{
            Rank = $_.Value
            Tag = Get-ContextTag $_.Key
            Path = $_.Key
        }
    } | Sort-Object Rank, Path | Select-Object -First $MaxResults)

    Write-Output "BHSpells context for '$Query' (Kind: $Kind)"
    Write-Output "Repository: $repoRoot"
    Write-Output "Search variants: $($variants -join ', ')"
    Write-Output ''

    if ($results.Count -eq 0) {
        Write-Warning 'No related files were found. Try -Kind All or a registry ID/class-name fragment.'
        return
    }

    foreach ($result in $results) {
        Write-Output ("[{0}] {1}" -f $result.Tag, $result.Path)
    }

    if ($ranked.Count -gt $results.Count) {
        Write-Output ''
        Write-Output ("Showing {0} of {1} matches. Increase -MaxResults to show more." -f $results.Count, $ranked.Count)
    }
}
finally {
    Pop-Location
}
