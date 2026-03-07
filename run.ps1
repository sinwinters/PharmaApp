# Скрипт запуска "Система управления аптекой"
# Проверяет зависимости, при необходимости догружает Java, Node.js и Gradle Wrapper

$ErrorActionPreference = "Stop"
[Console]::OutputEncoding = [System.Text.Encoding]::UTF8
$Host.UI.RawUI.WindowTitle = "PharmaApp - Запуск"

$ScriptDir = Split-Path -Parent $MyInvocation.MyCommand.Path
Set-Location $ScriptDir

$ToolsDir = Join-Path $ScriptDir ".pharma-app"
$Missing = @()
$Warnings = @()
$EnvBackend = @{}   # переменные для процесса backend (если что-то догрузили)
$EnvFrontend = @{}  # для frontend

# Каталоги проекта
if (-not (Test-Path "backend")) { $Missing += "Каталог backend не найден. Запускайте скрипт из корня проекта PharmaApp." }
if (-not (Test-Path "frontend")) { $Missing += "Каталог frontend не найден. Запускайте скрипт из корня проекта PharmaApp." }
if ($Missing.Count -gt 0) {
    Write-Host ""; Write-Host "ОШИБКА:" -ForegroundColor Red
    foreach ($m in $Missing) { Write-Host "  * $m" -ForegroundColor Red }; Write-Host ""; exit 1
}

# --- Создать каталог для догруженных компонентов ---
if (-not (Test-Path $ToolsDir)) { New-Item -ItemType Directory -Path $ToolsDir | Out-Null }

# ========== Java 21+ ==========
$NeedJava = $false
$JavaHome = $env:JAVA_HOME
try {
    $javaVersion = & java -version 2>&1 | Out-String
    if ($javaVersion -match 'version "(\d+)') {
        $major = [int]$Matches[1]
        if ($major -lt 21) { $NeedJava = $true }
    } else { $NeedJava = $true }
} catch { $NeedJava = $true }

if ($NeedJava) {
    $JdkDir = Join-Path $ToolsDir "jdk-21"
    if (Test-Path (Join-Path $JdkDir "bin\java.exe")) {
        $JavaHome = $JdkDir
        $env:JAVA_HOME = $JdkDir
        $env:PATH = "$JdkDir\bin;$env:PATH"
        Write-Host "Используется ранее загруженная Java 21: $JdkDir" -ForegroundColor Green
    } else {
        Write-Host "Загрузка Java 21 (Eclipse Temurin)..." -ForegroundColor Cyan
        $jdkZip = Join-Path $ToolsDir "openjdk-21.zip"
        try {
            [Net.ServicePointManager]::SecurityProtocol = [Net.SecurityProtocolType]::Tls12
            Invoke-WebRequest -Uri "https://api.adoptium.net/v3/binary/latest/21/ga/windows/x64/jdk/hotspot/normal/eclipse?project=jdk" -OutFile $jdkZip -UseBasicParsing
        } catch {
            $Missing += "Не удалось загрузить Java 21. Установите JDK 21 вручную: https://adoptium.net/"
        }
        if (Test-Path $jdkZip) {
            Expand-Archive -Path $jdkZip -DestinationPath $ToolsDir -Force
            $extracted = Get-ChildItem $ToolsDir -Directory | Where-Object { $_.Name -like "jdk*" -or $_.Name -like "OpenJDK*" } | Select-Object -First 1
            if ($extracted -and (Test-Path (Join-Path $extracted.FullName "bin\java.exe"))) {
                if ($extracted.FullName -ne $JdkDir) { Move-Item $extracted.FullName $JdkDir -Force -ErrorAction SilentlyContinue }
                $JavaHome = $JdkDir
                $env:JAVA_HOME = $JavaHome
                $env:PATH = "$JavaHome\bin;$env:PATH"
                Remove-Item $jdkZip -Force -ErrorAction SilentlyContinue
                Write-Host "Java 21 загружена: $JavaHome" -ForegroundColor Green
            }
        }
    }
}
if (-not $JavaHome) {
    try { $null = & java -version 2>&1 } catch { $Missing += "Java не найдена. Установите JDK 21: https://adoptium.net/" }
}

# ========== Node.js 18+ ==========
$NeedNode = $false
$NodeDir = $null
try {
    $nodeVersion = & node -v 2>&1
    if ($nodeVersion -match 'v(\d+)') { if ([int]$Matches[1] -lt 18) { $NeedNode = $true } } else { $NeedNode = $true }
} catch { $NeedNode = $true }

if ($NeedNode) {
    $NodeExtract = Join-Path $ToolsDir "node-20"
    if (Test-Path (Join-Path $NodeExtract "node.exe")) {
        $NodeDir = $NodeExtract
        $env:PATH = "$NodeDir;$env:PATH"
        Write-Host "Используется ранее загруженный Node: $NodeDir" -ForegroundColor Green
    } else {
        Write-Host "Загрузка Node.js 20 LTS..." -ForegroundColor Cyan
        $nodeZip = Join-Path $ToolsDir "node-20.zip"
        $nodeUrl = "https://nodejs.org/dist/v20.18.0/node-v20.18.0-win-x64.zip"
        try {
            [Net.ServicePointManager]::SecurityProtocol = [Net.SecurityProtocolType]::Tls12
            Invoke-WebRequest -Uri $nodeUrl -OutFile $nodeZip -UseBasicParsing
            Expand-Archive -Path $nodeZip -DestinationPath $ToolsDir -Force
            $nodeFolder = Get-ChildItem $ToolsDir -Directory | Where-Object { $_.Name -like "node-*" } | Select-Object -First 1
            if ($nodeFolder -and (Test-Path (Join-Path $nodeFolder.FullName "node.exe"))) {
                if ($nodeFolder.Name -ne "node-20") { Rename-Item $nodeFolder.FullName "node-20" -ErrorAction SilentlyContinue }
                $NodeDir = Join-Path $ToolsDir "node-20"
                $env:PATH = "$NodeDir;$env:PATH"
                Remove-Item $nodeZip -Force -ErrorAction SilentlyContinue
                Write-Host "Node.js загружен: $NodeDir" -ForegroundColor Green
            }
        } catch {
            $Missing += "Не удалось загрузить Node.js. Установите вручную: https://nodejs.org"
        }
    }
}
if ($NodeDir) { $EnvFrontend["PATH"] = "$NodeDir;$env:PATH" }

# ========== Gradle Wrapper ==========
$BackendDir = Join-Path $ScriptDir "backend"
$GradleCmd = $null
$GradleWrapperDir = Join-Path $BackendDir "gradle\wrapper"
if (Test-Path (Join-Path $BackendDir "gradlew.bat")) {
    $GradleCmd = ".\gradlew.bat"
} elseif (Test-Path (Join-Path $BackendDir "gradlew")) {
    $GradleCmd = ".\gradlew"
} else {
    $GradleDist = Join-Path $ToolsDir "gradle-8.10"
    $GradleBin = Join-Path $GradleDist "bin\gradle.bat"
    if (-not (Test-Path $GradleBin)) {
        Write-Host "Загрузка Gradle 8.10..." -ForegroundColor Cyan
        $gradleZip = Join-Path $ToolsDir "gradle-8.10-bin.zip"
        try {
            Invoke-WebRequest -Uri "https://services.gradle.org/distributions/gradle-8.10-bin.zip" -OutFile $gradleZip -UseBasicParsing
            Expand-Archive -Path $gradleZip -DestinationPath $ToolsDir -Force
            $g = Get-ChildItem $ToolsDir -Directory | Where-Object { $_.Name -like "gradle-*" } | Select-Object -First 1
            if ($g.Name -ne "gradle-8.10") { Rename-Item $g.FullName "gradle-8.10" -ErrorAction SilentlyContinue }
            Remove-Item $gradleZip -Force -ErrorAction SilentlyContinue
        } catch {
            $Missing += "Не удалось загрузить Gradle. В backend выполните: gradle wrapper --gradle-version 8.10"
        }
    }
    if (Test-Path $GradleBin) {
        Write-Host "Создание Gradle Wrapper в backend..." -ForegroundColor Cyan
        Push-Location $BackendDir
        & $GradleBin wrapper --gradle-version 8.10 2>&1 | Out-Null
        Pop-Location
        if (Test-Path (Join-Path $BackendDir "gradlew.bat")) {
            $GradleCmd = ".\gradlew.bat"
            Write-Host "Gradle Wrapper создан." -ForegroundColor Green
        }
    }
    if (-not $GradleCmd) {
        try { & gradle -version 2>&1 | Out-Null; $GradleCmd = "gradle" } catch {
            $Missing += "Gradle не найден и не удалось создать wrapper. Установите Gradle или выполните в backend: gradle wrapper --gradle-version 8.10"
        }
    }
}

# ========== Docker и порты PostgreSQL / Redis ==========
$DockerAvailable = $false
try { if ((& docker compose version 2>&1) -match 'version') { $DockerAvailable = $true } } catch { }

$PgPortOpen = $false; $RedisPortOpen = $false
try { $t = New-Object System.Net.Sockets.TcpClient; $t.Connect("127.0.0.1", 5432); $PgPortOpen = $true; $t.Close() } catch { }
try { $t = New-Object System.Net.Sockets.TcpClient; $t.Connect("127.0.0.1", 6379); $RedisPortOpen = $true; $t.Close() } catch { }

if (-not $PgPortOpen -or -not $RedisPortOpen) {
    if ($DockerAvailable) {
        Write-Host "Starting PostgreSQL and Redis via Docker..." -ForegroundColor Cyan
        & docker compose up -d postgres redis 2>&1 | Out-Null
        $attempts = 0
        while ($attempts -lt 30) {
            Start-Sleep -Seconds 1
            try { $t = New-Object System.Net.Sockets.TcpClient; $t.Connect("127.0.0.1", 5432); $t.Close(); break } catch { $attempts++ }
        }
        if ($attempts -ge 30) { $Missing += "PostgreSQL did not become healthy within 30 seconds. Check: docker compose logs postgres" }
        else { Write-Host "PostgreSQL and Redis are up." -ForegroundColor Green }
    } else {
        if (-not $PgPortOpen) { $Missing += "PostgreSQL is not reachable on localhost:5432. Install Docker and rerun this script, or install PostgreSQL 15+ (DB 'pharma', user/password 'pharma')." }
        if (-not $RedisPortOpen) { $Missing += "Redis is not reachable on localhost:6379. Install Docker or Redis manually." }
    }
}

# --- Критичные ошибки ---
if ($Missing.Count -gt 0) {
    Write-Host ""; Write-Host "ОШИБКА: Не удалось запустить приложение." -ForegroundColor Red; Write-Host ""
    Write-Host "Что отсутствует или не настроено:" -ForegroundColor Yellow
    foreach ($m in $Missing) { Write-Host "  * $m" -ForegroundColor Red }
    Write-Host ""; Write-Host "После исправления перезапустите: .\run.ps1" -ForegroundColor Cyan; exit 1
}

# --- Зависимости frontend ---
$FrontendDir = Join-Path $ScriptDir "frontend"
if (-not (Test-Path (Join-Path $FrontendDir "node_modules"))) {
    Write-Host "Установка зависимостей frontend (npm install)..." -ForegroundColor Cyan
    Push-Location $FrontendDir
    & npm install 2>&1 | Out-Null
    Pop-Location
    if ($LASTEXITCODE -ne 0) { Write-Host "Ошибка npm install." -ForegroundColor Red; exit 1 }
    Write-Host "Зависимости frontend установлены." -ForegroundColor Green
}

# --- Команда для backend (с подставленным JAVA_HOME при догрузке) ---
$backendCmd = "Set-Location '$BackendDir'"
if ($JavaHome) { $backendCmd += "; `$env:JAVA_HOME = '$JavaHome'; `$env:PATH = '$JavaHome\bin;' + `$env:PATH" }
$backendCmd += "; if (Test-Path '.\gradlew.bat') { .\gradlew.bat bootRun } elseif (Test-Path '.\gradlew') { .\gradlew bootRun } else { gradle bootRun }"

Write-Host ""; Write-Host "Запуск Backend в отдельном окне..." -ForegroundColor Cyan
Start-Process powershell -ArgumentList "-NoExit", "-Command", $backendCmd

Write-Host "Ожидание старта backend (~25 сек)..." -ForegroundColor Cyan
Start-Sleep -Seconds 25
try {
    $r = Invoke-WebRequest -Uri "http://localhost:8080/api/v1/auth/login" -Method POST -ContentType "application/json" -Body '{"username":"admin","password":"password"}' -UseBasicParsing -TimeoutSec 5
    if ($r.StatusCode -eq 200) { Write-Host "Backend запущен." -ForegroundColor Green }
} catch {
    Write-Host "Backend ещё стартует или порт 8080 занят. Через минуту: http://localhost:8080/api/v1/swagger-ui.html" -ForegroundColor Yellow
}

$frontendCmd = "Set-Location '$FrontendDir'"
if ($NodeDir) { $frontendCmd += "; `$env:PATH = '$NodeDir;' + `$env:PATH" }
$frontendCmd += "; npm run dev"

Write-Host "Запуск Frontend в отдельном окне..." -ForegroundColor Cyan
Start-Process powershell -ArgumentList "-NoExit", "-Command", $frontendCmd

Write-Host ""
Write-Host "========================================" -ForegroundColor Green
Write-Host "  Приложение запущено." -ForegroundColor Green
Write-Host "========================================" -ForegroundColor Green
Write-Host "  Frontend:  http://localhost:5173" -ForegroundColor White
Write-Host "  Backend:   http://localhost:8080/api/v1" -ForegroundColor White
Write-Host "  Демо-вход: admin / password" -ForegroundColor Cyan
Write-Host ""
Write-Host "Догруженные компоненты лежат в .pharma-app (можно удалить при ручной установке)." -ForegroundColor Gray
Write-Host "Закройте окна Backend и Frontend для остановки." -ForegroundColor Gray
Write-Host ""
