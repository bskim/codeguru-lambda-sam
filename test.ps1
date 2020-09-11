$i = 1
$number = 300
do{
    curl "https://3yht482qpi.execute-api.us-west-2.amazonaws.com/hello"
    sleep 10
    $i++
    echo $i
}

while ($i -le $number)