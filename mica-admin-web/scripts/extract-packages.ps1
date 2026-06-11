# Extract @vben/* and @vben-core/* packages from vue-vben-admin monorepo
$srcRoot = "E:\codes\ai\vue-vben-admin"
$destRoot = "E:\codes\ai\mica-less\new-ui"

# Package definitions: sourceRelativePath, targetName, dependencies (npm)
$pkgs = @(
  # === @vben-core/* (bottom layer) ===
  @{
    src = "packages\@core\base\shared"
    name = "@vben-core/shared"
    deps = @("@ctrl/tinycolor", "@tanstack/vue-store", "@vue/shared", "clsx", "dayjs", "defu", "es-toolkit", "lodash.clonedeep", "nprogress", "tailwind-merge", "theme-colors")
    dir = "_vben/core/shared"
  }
  @{
    src = "packages\@core\base\typings"
    name = "@vben-core/typings"
    deps = @("vue", "vue-router")
    dir = "_vben/core/typings"
  }
  @{
    src = "packages\@core\base\icons"
    name = "@vben-core/icons"
    deps = @("@iconify/vue", "lucide-vue-next", "vue")
    dir = "_vben/core/icons"
  }
  @{
    src = "packages\@core\composables"
    name = "@vben-core/composables"
    deps = @("@vben-core/shared", "@vueuse/core", "reka-ui", "sortablejs", "vue")
    dir = "_vben/core/composables"
  }
  @{
    src = "packages\@core\preferences"
    name = "@vben-core/preferences"
    deps = @("@vben-core/shared", "@vben-core/typings", "@vueuse/core", "vue")
    dir = "_vben/core/preferences"
  }
  @{
    src = "packages\@core\ui-kit\design"
    name = "@vben-core/design"
    deps = @()
    dir = "_vben/core/design"
  }
  @{
    src = "packages\@core\ui-kit\form-ui"
    name = "@vben-core/form-ui"
    deps = @("@vben-core/composables", "@vben-core/icons", "@vben-core/shadcn-ui", "@vben-core/shared", "@vben-core/typings", "@vee-validate/zod", "@vueuse/core", "vee-validate", "vue", "zod", "zod-defaults")
    dir = "_vben/core/form-ui"
  }
  @{
    src = "packages\@core\ui-kit\layout-ui"
    name = "@vben-core/layout-ui"
    deps = @("@vben-core/composables", "@vben-core/icons", "@vben-core/shadcn-ui", "@vben-core/shared", "@vben-core/typings", "@vueuse/core", "vue")
    dir = "_vben/core/layout-ui"
  }
  @{
    src = "packages\@core\ui-kit\menu-ui"
    name = "@vben-core/menu-ui"
    deps = @("@vben-core/composables", "@vben-core/design", "@vben-core/icons", "@vben-core/shadcn-ui", "@vben-core/shared", "@vben-core/typings", "@vueuse/core", "qs", "vue")
    dir = "_vben/core/menu-ui"
  }
  @{
    src = "packages\@core\ui-kit\popup-ui"
    name = "@vben-core/popup-ui"
    deps = @("@vben-core/composables", "@vben-core/icons", "@vben-core/preferences", "@vben-core/shadcn-ui", "@vben-core/shared", "@vben-core/typings", "@vueuse/core", "vue")
    dir = "_vben/core/popup-ui"
  }
  @{
    src = "packages\@core\ui-kit\shadcn-ui"
    name = "@vben-core/shadcn-ui"
    deps = @("@vben-core/composables", "@vben-core/design", "@vben-core/icons", "@vben-core/shared", "@vben-core/typings", "@vueuse/core", "class-variance-authority", "lucide-vue-next", "reka-ui", "vee-validate", "vue")
    dir = "_vben/core/shadcn-ui"
  }
  @{
    src = "packages\@core\ui-kit\tabs-ui"
    name = "@vben-core/tabs-ui"
    deps = @("@vben-core/composables", "@vben-core/design", "@vben-core/icons", "@vben-core/shadcn-ui", "@vben-core/typings", "@vueuse/core", "vue")
    dir = "_vben/core/tabs-ui"
  }

  # === @vben/* (mid layer) ===
  @{
    src = "packages\types"
    name = "@vben/types"
    deps = @("@vben-core/typings", "vue", "vue-router")
    dir = "_vben/packages/types"
  }
  @{
    src = "packages\utils"
    name = "@vben/utils"
    deps = @("@vben-core/shared", "@vben-core/typings", "vue-router")
    dir = "_vben/packages/utils"
  }
  @{
    src = "packages\constants"
    name = "@vben/constants"
    deps = @("@vben-core/shared")
    dir = "_vben/packages/constants"
  }
  @{
    src = "packages\preferences"
    name = "@vben/preferences"
    deps = @("@vben-core/preferences", "@vben-core/typings")
    dir = "_vben/packages/preferences"
  }
  @{
    src = "packages\stores"
    name = "@vben/stores"
    deps = @("@vben-core/preferences", "@vben-core/shared", "@vben-core/typings", "pinia", "pinia-plugin-persistedstate", "secure-ls", "vue", "vue-router")
    dir = "_vben/packages/stores"
  }
  @{
    src = "packages\effects\request"
    name = "@vben/request"
    deps = @("@vben/locales", "@vben/utils", "axios", "qs")
    dir = "_vben/packages/request"
  }
  @{
    src = "packages\effects\access"
    name = "@vben/access"
    deps = @("@vben/preferences", "@vben/stores", "@vben/types", "@vben/utils", "vue")
    dir = "_vben/packages/access"
  }
  @{
    src = "packages\effects\hooks"
    name = "@vben/hooks"
    deps = @("@vben-core/composables", "@vben/preferences", "@vben/stores", "@vben/types", "@vben/utils", "@vueuse/core", "vue", "vue-router", "watermark-js-plus")
    dir = "_vben/packages/hooks"
  }
  @{
    src = "packages\locales"
    name = "@vben/locales"
    deps = @("@intlify/core-base", "@vben-core/composables", "vue", "vue-i18n")
    dir = "_vben/packages/locales"
  }
  @{
    src = "packages\icons"
    name = "@vben/icons"
    deps = @("@vben-core/icons")
    dir = "_vben/packages/icons"
  }
  @{
    src = "packages\styles"
    name = "@vben/styles"
    deps = @("@vben-core/design")
    dir = "_vben/packages/styles"
  }
  @{
    src = "packages\effects\plugins"
    name = "@vben/plugins"
    deps = @("@vben-core/design", "@vben-core/form-ui", "@vben-core/popup-ui", "@vben-core/shadcn-ui", "@vben-core/shared", "@vben/hooks", "@vben/icons", "@vben/locales", "@vben/preferences", "@vben/types", "@vben/utils", "@vueuse/core", "@vueuse/motion", "vue", "vxe-pc-ui", "vxe-table", "echarts")
    dir = "_vben/packages/plugins"
  }
  @{
    src = "packages\effects\layouts"
    name = "@vben/layouts"
    deps = @("@vben-core/composables", "@vben-core/design", "@vben-core/form-ui", "@vben-core/layout-ui", "@vben-core/menu-ui", "@vben-core/popup-ui", "@vben-core/shadcn-ui", "@vben-core/shared", "@vben-core/tabs-ui", "@vben/constants", "@vben/hooks", "@vben/icons", "@vben/locales", "@vben/preferences", "@vben/stores", "@vben/types", "@vben/utils", "@vueuse/core", "vue", "vue-router")
    dir = "_vben/packages/layouts"
  }
  @{
    src = "packages\effects\common-ui"
    name = "@vben/common-ui"
    deps = @("@vben-core/design", "@vben-core/form-ui", "@vben-core/popup-ui", "@vben-core/preferences", "@vben-core/shadcn-ui", "@vben-core/shared", "@vben/constants", "@vben/hooks", "@vben/icons", "@vben/locales", "@vben/types", "@vueuse/core", "@vueuse/integrations", "json-bigint", "qrcode", "tippy.js", "vue", "vue-json-pretty", "vue-router", "vue-tippy")
    dir = "_vben/packages/common-ui"
  }
)

foreach ($pkg in $pkgs) {
  $srcDir = "$srcRoot\$($pkg.src)\src"
  $destDir = "$destRoot\$($pkg.dir)"
  $destSrcDir = "$destDir\src"
  $pkgJsonPath = "$destDir\package.json"

  Write-Host "---"
  Write-Host "Extracting $($pkg.name)..."
  Write-Host "  Source: $srcDir"
  Write-Host "  Dest:   $destDir"

  # Create directory
  New-Item -ItemType Directory -Force -Path $destSrcDir | Out-Null

  # Copy source files
  if (Test-Path $srcDir) {
    Copy-Item -Path "$srcDir\*" -Destination $destSrcDir -Recurse -Force -ErrorAction SilentlyContinue
    $fileCount = (Get-ChildItem -Recurse -File $destSrcDir | Measure-Object).Count
    Write-Host "  Copied $fileCount files"
  } else {
    Write-Host "  WARNING: Source directory not found: $srcDir"
  }

  # Create package.json
  # Filter out workspace deps (@vben*, @vben-core*) from the deps list
  $npmDeps = $pkg.deps | Where-Object { $_ -notmatch '^@vben' }
  $workspaceDeps = $pkg.deps | Where-Object { $_ -match '^@vben' }

  $pkgJson = @{
    name = $pkg.name
    version = "0.0.1"
    private = $true
    type = "module"
    main = "./src/index.ts"
    types = "./src/index.ts"
    exports = @{
      "." = "./src/index.ts"
      "./*" = "./src/*"
    }
  }

  $depsObj = @{}
  foreach ($dep in $npmDeps) {
    $depsObj[$dep] = "*"
  }
  foreach ($dep in $workspaceDeps) {
    $depsObj[$dep] = "workspace:*"
  }
  if ($depsObj.Keys.Count -gt 0) {
    $pkgJson.dependencies = $depsObj
  }

  $pkgJson | ConvertTo-Json -Depth 5 | Set-Content -Path $pkgJsonPath -Encoding UTF8
  Write-Host "  package.json created with $($npmDeps.Count) npm deps + $($workspaceDeps.Count) workspace deps"
}

Write-Host "=== Done ==="
