<?php
include('config.php');
function rrmdir($dir) {
    foreach(glob($dir . '/' . '*') as $file) {
        if(is_dir($file)){
            rrmdir($file);
        }else{
            unlink($file);
        }
    }
    rmdir($dir);
}

function get_data($deviceID,$name,$picname) {
	include('config.php');
	$ch = curl_init();

	curl_setopt($ch, CURLOPT_URL, FACEAPI. FACEANA);
	curl_setopt($ch, CURLOPT_RETURNTRANSFER, 1);
	curl_setopt($ch, CURLOPT_POSTFIELDS, 'device='.$deviceID.'&person='.$name.'&picname='.$picname);
	curl_setopt($ch, CURLOPT_POST, 1);

	$headers = array();
	$headers[] = "Content-Type: application/x-www-form-urlencoded";
	curl_setopt($ch, CURLOPT_HTTPHEADER, $headers);

	$data = curl_exec($ch);
	if (curl_errno($ch)) throw new Exception("Failed when requesting face api!");
	
	curl_close ($ch);

	return $data;
}

function get_name($deviceID,$picname,$people) {
	include('config.php');
	$people = json_encode($people);
	if(DEBUG) {
		//echo $people;
	}
	$ch = curl_init();

	curl_setopt($ch, CURLOPT_URL, FACEAPI. FACEREC);
	curl_setopt($ch, CURLOPT_RETURNTRANSFER, 1);
	curl_setopt($ch, CURLOPT_POSTFIELDS, 'device='.$deviceID.'&picname='.$picname.'&people='.$people);
	curl_setopt($ch, CURLOPT_POST, 1);

	$headers = array();
	$headers[] = "Content-Type: application/x-www-form-urlencoded";
	curl_setopt($ch, CURLOPT_HTTPHEADER, $headers);

	$data = curl_exec($ch);
	if (curl_errno($ch)) throw new Exception("Failed when requesting face api!");
	
	curl_close ($ch);

	$data = json_decode($data);
	$result = $data -> {'name'} . ',' . (string) $data -> {'confidence'} . '%';

	return $result;
}

?>