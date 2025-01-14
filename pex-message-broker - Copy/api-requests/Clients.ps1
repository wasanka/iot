# ParentScript.ps1

# Path to the child script
$childScriptPath = "D:\MyStuff\SourceCode\EmbeddedMQ\pex-message-broker\api-requests\client1.ps1"

# Number of parallel executions
$numRuns = 10

# Array to store job information
$jobs = @()

# Start the child script in parallel 10 times
for ($i = 1; $i -le $numRuns; $i++) {
    $job = Start-Job -ScriptBlock {
        param($scriptPath)
        # Run the child script
        & $scriptPath
    } -ArgumentList $childScriptPath

    # Store job info
    $jobs += $job
}

# Wait for all jobs to complete
$jobs | ForEach-Object {
    $job = $_
    Wait-Job -Job $job
    # Retrieve and display job output
    Receive-Job -Job $job | Write-Output
}

# Clean up jobs
$jobs | Remove-Job
