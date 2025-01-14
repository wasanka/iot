$url = "http://localhost:8001/message?queue=test_queue&clientId=EFG1"


while ($true) {
    $response = Invoke-WebRequest -Uri $url -UseBasicParsing
    if ($response.StatusCode -eq 200) {
        $json = $response | ConvertFrom-Json
        $value = $json.parameters.messageId # Replace 'someProperty' with the actual property name you want to extract
        $body = $json.body # Replace 'someProperty' with the actual property name you want to extract
        Write-Output "Message Id: $value Content: $body"

        $url2 = "http://localhost:8001/message?queue=test_queue&messageId=$value&clientId=EFG1"
        Invoke-WebRequest -Uri $url2 -Method DELETE

    }
#     Start-Sleep -Milliseconds 500
}

