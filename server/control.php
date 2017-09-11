<?php
include('common.php');
require('dbManager.php');

class Controler {
	private function __construct() {}

	public function run($method,$deviceID,$value) {
		$info = array(
			'status' => 0,
			'message' => 'Not set'
			);

		if(empty($method) || empty($deviceID) || empty($value)) {
			$info['message'] = 'Please check your request params';
			throw new Exception(json_encode($info));
		}
		$dbManager = dbManager::getInstance();
		switch ($method) {
			case 'addNewDevice':
				$info['status'] = (int) ($dbManager->isDeviceIn($deviceID) || $dbManager->addNewDevice($deviceID));
				if($info['status']) {
					mkdir(BASEDIR. $deviceID);
					mkdir(BASEDIR. $deviceID. DIVIDE. UNNAMED);
					$info['message'] = "New device successfully added";
				} else {
					$info['message'] = "Failed to add new device";
				}
				break;

			case 'insert':
				$info['status'] = $dbManager->insert($deviceID,$value);
				if($info['status']) {
					mkdir(BASEDIR. $deviceID. DIVIDE. $value);
					$info['message'] = "New person successfully added";
				} else {
					$info['message'] = "Failed to add new person";
				}
				break;

			case 'delete':
				$info['status'] = $dbManager->delete($deviceID,$value);
				if($info['status']) {
					rrmdir(BASEDIR. $deviceID. DIVIDE. $value);
					$info['message'] = "Successfully deleted";
				} else {
					$info['message'] = "Failed to delete";
				}
				break;

			case 'clear':
				$info['status'] = $dbManager->clear($deviceID);
				if($info['status']) {
					rrmdir(BASEDIR. $deviceID);
					self::run('addNewDevice',$deviceID,$value);
					$info['message'] = "Successfully cleared";
				} else {
					$info['message'] = "Failed to clear";
				}
				break;

			case 'cleargal':
				$info['status'] = $dbManager->cleargal($deviceID,$value);
				if($info['status']) {
					$files = glob(BASEDIR. $deviceID. DIVIDE. $value. DIVIDE.'*');
					foreach($files as $file){
						if(is_file($file))
						unlink($file);
					}
					$info['message'] = "Successfully cleared";
				} else {
					$info['message'] = "Failed to clear";
				}
				break;

			case 'update':
				$person = json_decode($value,true);
				$info['status'] = $dbManager->remove_photo($person['picname'], $deviceID, $person['name']);
				if($info['status']) {
					unlink(BASEDIR. $deviceID. DIVIDE. $person['name']. DIVIDE. $person['picname']);
					$info['message'] = "Successfully removed";
				} else {
					$info['message'] = "Failed to remove";
				}
				break;

			case 'ask':
				$result = 'Not known';
				try{
					$people = $dbManager -> fetch_person($deviceID);
					$people_data = array();
					foreach ($people as &$person) {
						$people_data[$person] = $dbManager -> fetch_data($deviceID,$person);
					}
					$result = get_name($deviceID,$value,$people_data);
				} catch(Exception $e) {
					$info['status'] = 0;
					$info['message'] = $e -> getMessage();
					throw new Exception(json_encode($info));
				}
				$info['status'] = 1;
				$info['message'] = $result;

			default:
				break;
		}
		return json_encode($info);
	}
}

if(!DEBUG) {
	$method = (string)$_POST['method'];
	$deviceID = (string)$_POST['deviceID'];
	$value = (string)$_POST['value'];
} else{
	$method = 'addNewDevice';
	$deviceID = 'd41d8cd98f00b204e9800998ecf8427e';
	$value = 'aligned_1505116365938.jpg';
}

try{
	$info = Controler::run($method,$deviceID,$value);
	echo $info;
}catch(Exception $e){
	die($e -> getMessage());
}