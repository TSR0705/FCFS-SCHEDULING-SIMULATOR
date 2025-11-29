# PowerShell script to push all remaining files to GitHub repo, one file per commit

Write-Host "Starting to push all remaining files to GitHub repo..." -ForegroundColor Green

# Function to commit and push a single file
function Commit-And-Push-File {
    param (
        [string]$file,
        [string]$commitMessage
    )
    
    # Check if file has changes
    $status = git status --porcelain $file
    if ([string]::IsNullOrEmpty($status)) {
        Write-Host "Skipping $file (no changes)" -ForegroundColor Yellow
        return
    }
    
    # Add file
    git add $file
    
    # Commit file
    git commit -m "$commitMessage"
    
    # Push file
    $result = git push origin main
    if ($LASTEXITCODE -eq 0) {
        Write-Host "Successfully pushed $file" -ForegroundColor Green
    } else {
        Write-Host "Failed to push $file" -ForegroundColor Red
        return 1
    }
}

# Get all files except ignored ones
$files = Get-ChildItem -Recurse -File | Where-Object {
    $_.FullName -notlike "*\target\*" -and
    $_.FullName -notlike "*\out\*" -and
    $_.FullName -notlike "*\build\*" -and
    $_.FullName -notlike "*\.idea\*" -and
    $_.FullName -notlike "*\.vscode\*" -and
    $_.FullName -notlike "*\node_modules\*" -and
    $_.Name -ne ".DS_Store" -and
    $_.Extension -ne ".class" -and
    $_.Name -ne "push_files.ps1" -and
    $_.Name -ne "push_files.sh" -and
    $_.Name -ne "README.md"
} | Sort-Object FullName

foreach ($file in $files) {
    $relativePath = Resolve-Path -Relative $file.FullName
    $relativePath = $relativePath.Substring(2) # Remove the ".\" prefix
    
    # Get filename and extension
    $filename = $file.Name
    $extension = $file.Extension.TrimStart(".")
    $basenameNoExt = [System.IO.Path]::GetFileNameWithoutExtension($file.Name)
    
    # Generate commit message based on file type
    switch ($extension) {
        "java" {
            if ($filename -eq "module-info.java") {
                $commitMsg = "Add module descriptor"
            } else {
                $commitMsg = "Add $basenameNoExt"
            }
        }
        {($_ -eq "png") -or ($_ -eq "jpg") -or ($_ -eq "jpeg") -or ($_ -eq "gif")} {
            $commitMsg = "Add screenshot $filename"
        }
        "md" {
            $commitMsg = "Add/update documentation $filename"
        }
        "xml" {
            if ($filename -eq "pom.xml") {
                $commitMsg = "Add Maven project configuration"
            } else {
                $commitMsg = "Add configuration file $filename"
            }
        }
        "fxml" {
            $commitMsg = "Add FXML view $filename"
        }
        {($_ -eq "cmd") -or ($_ -eq "ps1")} {
            $commitMsg = "Add script $filename"
        }
        default {
            $commitMsg = "Add $filename"
        }
    }
    
    Write-Host "Processing $relativePath" -ForegroundColor Yellow
    Commit-And-Push-File -file $relativePath -commitMessage $commitMsg
}

Write-Host "Finished pushing all files!" -ForegroundColor Green