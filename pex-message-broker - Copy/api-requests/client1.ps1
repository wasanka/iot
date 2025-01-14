param (
    [string]$clientId="ABC",
    [string]$url = "http://localhost:8000/message?queue=test_queue&clientId=$clientId"  # URL to send the request to
)

while ($true) {
    $response = Invoke-WebRequest -Uri $url -UseBasicParsing
    if ($response.StatusCode -eq 200) {
        $json = $response | ConvertFrom-Json
        $value = $json.parameters.messageId # Replace 'someProperty' with the actual property name you want to extract
        $body = $json.body # Replace 'someProperty' with the actual property name you want to extract
        Write-Output "Message Id: $value Content: $body"

        $url2 = "http://localhost:8000/message?queue=test_queue&messageId=$value&clientId=$clientId"

        Invoke-WebRequest -Uri $url2 -Method DELETE
    }
}

