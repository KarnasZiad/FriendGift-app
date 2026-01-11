$ErrorActionPreference='Stop'

$repo = Split-Path -Parent $PSScriptRoot
$backend = Join-Path $repo 'backend'
$keysDir = Join-Path $backend 'keys'
New-Item -ItemType Directory -Path $keysDir -Force | Out-Null

$genJava = Join-Path $backend 'tools\GenerateJwtKeys.java'
if (-not (Test-Path $genJava)) {
	throw "Missing $genJava"
}

Write-Host "Generating JWT RSA keys in $keysDir" -ForegroundColor Cyan

Push-Location (Join-Path $backend 'tools')
try {
	# Compile tool (output ignored by .gitignore: *.class)
	javac .\GenerateJwtKeys.java
	java -cp . GenerateJwtKeys (Join-Path $keysDir 'privateKey.pem') (Join-Path $keysDir 'publicKey.pem')
} finally {
	Pop-Location
}

Write-Host "Done." -ForegroundColor Green
Write-Host "- Private: backend/keys/privateKey.pem" 
Write-Host "- Public : backend/keys/publicKey.pem" 
