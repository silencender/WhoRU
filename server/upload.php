<?php
include('common.php');
require('dbManager.php');

$BASEDIR = './photos/';
$DIVIDE = '/';
$dbManager = dbManager::getInstance();

$info = array(
	'status' => 0,
	'message' => 'Not set'
	);
$deviceID = $_POST['device'];
$name = $_POST['name'];
if(empty(trim($deviceID)) || empty(trim($name))) {
	$info['message'] = 'Please check your request params';
	die(json_encode($info));
}
$target_path = $BASEDIR. $deviceID. $DIVIDE. $name. $DIVIDE. basename( $_FILES['photo']['name']);

$data = 'aaa';

if($_FILES['photo']['type'] === 'image/jpeg'
	&& strpos($_FILES['photo']['name'],'.jpg') !== false
	&& strpos($_FILES['photo']['name'],'php') === false) {
	if($dbManager-> add_photo($_FILES['photo']['name'],$deviceID,$name,$data) &&
			 move_uploaded_file($_FILES['photo']['tmp_name'], $target_path)) {
	   $info['status'] = 1;
	   $info['message'] = "The file ".  basename( $_FILES['photo']['name']). " has been uploaded";
	}  else{
		$info['status'] = 0;
		$info['message'] = "There was an error uploading the file, please try again!" . $_FILES['photo']['error'];
	}
} else {
	$info['status'] = 0;
	$info['message'] = "Wrong file type!";
}
echo json_encode($info);

?>