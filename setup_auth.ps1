# PowerShell script to help set up Minecraft authentication
Write-Host "Setting up Minecraft authentication for development..." -ForegroundColor Green
Write-Host ""
Write-Host "To get your authentication tokens safely:" -ForegroundColor Yellow
Write-Host "1. UUID: Visit https://mcuuid.net/ and enter your Minecraft username" -ForegroundColor White
Write-Host "2. Access Token: Use the official Minecraft launcher (recommended)" -ForegroundColor White
Write-Host "3. Or set environment variables:" -ForegroundColor White
Write-Host '   $env:MC_USERNAME="YourUsername"' -ForegroundColor Cyan
Write-Host '   $env:MC_UUID="YourUUID"' -ForegroundColor Cyan
Write-Host '   $env:MC_ACCESS_TOKEN="YourAccessToken"' -ForegroundColor Cyan
Write-Host ""
Write-Host "Then run: ./gradlew runClient" -ForegroundColor Green