[CmdletBinding()]
param($H, $services=$(throw "services parameter is required."))

$files=""
foreach ($service In $services) {
	$file="$service-docker-compose.yml"
	if(Test-Path -Path $file){
        $files="$files -f $file"
        if ($service == "dashboard") {
            $files="$files -f mongo-docker-compose.yml -f keycloak-docker-compose.yml"
        } 
        if ($service == "edge") {
            $files="$files -f nexus-docker-compose.yml -f kafka-docker-compose.yml"
        }
	} else {
		echo ("File $file does not exist")
	}
}
if (-not ([string]::IsNullOrEmpty($files)))
{
	if ([string]::IsNullOrEmpty($H)) {
		$CMD="docker-compose $files up"
	} else {
		$CMD="docker-compose -H $H $files up"
	}
	
	echo $CMD
	iex $CMD
} else {
	echo "No available component to execute"
}

