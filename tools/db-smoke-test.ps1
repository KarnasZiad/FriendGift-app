$ErrorActionPreference='Stop'

$repo = Split-Path -Parent $PSScriptRoot
$api = 'http://localhost:8080'
$logOut = Join-Path $repo 'backend-dev.log'
$logErr = Join-Path $repo 'backend-dev.err.log'

function Wait-BackendUp {
	param([int]$TimeoutSeconds = 180)
	$deadline = (Get-Date).AddSeconds($TimeoutSeconds)
	while ((Get-Date) -lt $deadline) {
		try {
			$resp = Invoke-RestMethod -Method Post -Uri "$api/api/auth/login" -ContentType 'application/json' -Body '{"username":"omar","password":"password"}'
			if ($resp.token) { return $resp.token }
		} catch {
			Start-Sleep -Milliseconds 900
		}
	}
	Write-Host "Backend not ready after $TimeoutSeconds seconds. Log tails:" -ForegroundColor Yellow
	if (Test-Path $logOut) {
		Write-Host "--- $logOut (tail) ---" -ForegroundColor Yellow
		Get-Content -Path $logOut -Tail 60 -ErrorAction SilentlyContinue
	}
	if (Test-Path $logErr) {
		Write-Host "--- $logErr (tail) ---" -ForegroundColor Yellow
		Get-Content -Path $logErr -Tail 60 -ErrorAction SilentlyContinue
	}
	throw "Backend not ready after $TimeoutSeconds seconds"
}

function Start-Backend {
	Remove-Item $logOut,$logErr -ErrorAction SilentlyContinue
	$backendDir = Join-Path $repo 'backend'
	Start-Process -FilePath 'cmd.exe' -ArgumentList @('/c','mvn','quarkus:dev') -WorkingDirectory $backendDir -RedirectStandardOutput $logOut -RedirectStandardError $logErr -PassThru
}

function Stop-Backend([int]$ProcessId) {
	Stop-Process -Id $ProcessId -Force -ErrorAction SilentlyContinue
}

$name = 'DB Smoke Test ' + (Get-Date -Format 'HHmmss')

Write-Host 'Starting backend (dev)...'
$p1 = Start-Backend
try {
	$token1 = Wait-BackendUp
	Write-Host "Backend is up. Creating friend: $name"

	$created = Invoke-RestMethod -Method Post -Uri "$api/api/friends" -Headers @{ Authorization = "Bearer $token1" } -ContentType 'application/json' -Body (@{ name = $name } | ConvertTo-Json)
	if (-not $created.id) { throw 'Friend creation did not return an id' }
	Write-Host ("Created friend id=" + $created.id)
} finally {
	Write-Host 'Stopping backend...'
	Stop-Backend $p1.Id
	Start-Sleep -Seconds 3
}

$dbDir = Join-Path (Join-Path $repo 'backend') 'data'
$dbFiles = Get-ChildItem -Path $dbDir -ErrorAction SilentlyContinue | Where-Object { $_.Name -like 'friendgift*' }
if (-not $dbFiles) {
	Write-Warning "No DB file found in $dbDir yet. If this keeps happening, check $logOut / $logErr"
} else {
	Write-Host ('DB files: ' + ($dbFiles.Name -join ', '))
}

Write-Host 'Restarting backend (dev)...'
$p2 = Start-Backend
try {
	$token2 = Wait-BackendUp
	$friends = Invoke-RestMethod -Method Get -Uri "$api/api/friends" -Headers @{ Authorization = "Bearer $token2" }
	$names = @($friends | ForEach-Object { $_.name })

	if ($names -contains $name) {
		Write-Host 'PASS: Friend still present after restart (H2 persistence works).'
	} else {
		throw 'FAIL: Friend not found after restart (persistence not working as expected).'
	}
} finally {
	Write-Host 'Stopping backend...'
	Stop-Backend $p2.Id
}
