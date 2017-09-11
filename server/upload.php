<?php
include('common.php');
require('dbManager.php');

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
$target_path = BASEDIR. $deviceID. DIVIDE. $name. DIVIDE. basename( $_FILES['photo']['name']);

if($_FILES['photo']['type'] === 'image/jpeg'
	&& strpos($_FILES['photo']['name'],'.jpg') !== false
	&& strpos($_FILES['photo']['name'],'php') === false) {
	if(move_uploaded_file($_FILES['photo']['tmp_name'], $target_path)) {
		if($name != UNNAMED){
			$data = '';
			try{
				$data = get_data($deviceID,$name,$_FILES['photo']['name']);
			} catch(Exception $e) {
				$info['status'] = 0;
				$info['message'] = $e -> getMessage();
				die(json_encode($info));
			}
			if($dbManager-> add_photo($_FILES['photo']['name'],$deviceID,$name,$data)) {
				$info['status'] = 1;
				$info['message'] = "The file ".  basename( $_FILES['photo']['name']). " has been uploaded";
			}
		} else {
			$info['status'] = 1;
			$info['message'] = "The file ".  basename( $_FILES['photo']['name']). " has been uploaded";
		}
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