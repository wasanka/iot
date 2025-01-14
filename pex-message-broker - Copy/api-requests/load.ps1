param (
    [string]$clientId="ABCD",
    [int]$count=1000,  # Number of times to send the request
    [string]$url = "http://localhost:8000/message?queue=test_queue&clientId=$clientId",  # URL to send the request to
    [string]$body = 'Message content'  # JSON body for the POST request
)

for ($i = 1; $i -le $count; $i++) {
    Invoke-WebRequest -Uri $url -Method POST -Body "Message $i" -ContentType "application/text"
}

Write-Output "Completed $count POST requests to $url"
