<?php

class dbManager {
	private static $instance;
	private static $config = array(
			'dbhost' => 'localhost',
			'dbusername' => 'prp',
			'dbname' => 'prp',
			'dbpwd' => 'prp1518407'
		);
	private static $conn;
	private static $addNewDevice;
	private static $isDeviceIn;
	private static $insert;
	private static $delete;
	private static $clear;
	private static $cleargal;
	private static $add_photo;
	private static $remove_photo;
	private static $fetch_person;
	private static $fetch_data;

	public function __construct() {
		self::$conn = new mysqli(self::$config['dbhost'],self::$config['dbusername'],
								self::$config['dbpwd'],self::$config['dbname']);
		if (self::$conn->connect_error) {
    		throw new Exception("Connection failed: " . $conn->connect_error);
		}
		self::$conn -> set_charset("utf8");
		self::$addNewDevice = self::$conn->prepare("INSERT INTO prp_device (device) VALUES (?)");
		self::$isDeviceIn = self::$conn->prepare("SELECT COUNT(id) FROM prp_device WHERE device = ?");
		self::$insert = self::$conn->prepare("INSERT INTO prp_person (device, person) 
												VALUES ((SELECT id FROM prp_device WHERE device = ?), ?)");
		self::$delete = self::$conn->prepare("DELETE FROM prp_person WHERE (device,person) = 
												((SELECT id FROM prp_device WHERE device = ?),?)");
		self::$clear = self::$conn->prepare("DELETE FROM prp_device WHERE device = ?");
		self::$cleargal = self::$conn->prepare("DELETE FROM prp_image WHERE person = 
													(SELECT id FROM prp_person WHERE (device,person) = 
														((SELECT id FROM prp_device WHERE device = ?),?))");
		self::$add_photo = self::$conn->prepare("INSERT INTO prp_image (title,person,data) VALUES
												(?,(SELECT id FROM prp_person WHERE (device,person) = 
													((SELECT id FROM prp_device WHERE device = ?),?)
														),?)");
		self::$remove_photo = self::$conn->prepare("DELETE FROM prp_image WHERE title = ?");
		self::$fetch_person = self::$conn->prepare("SELECT person FROM prp_person WHERE device = 
													(SELECT id FROM prp_device WHERE device = ?)");
		self::$fetch_data = self::$conn->prepare("SELECT data FROM prp_image WHERE person =
													(SELECT id FROM prp_person WHERE (device,person) = 
														((SELECT id FROM prp_device WHERE device = ?),?))");
	}

	public function getInstance() {
		if(self::$instance == null){
			try{
				self::$instance = new self();
			}catch(Exception $e) {
				die($e->getMessage());
			}
		}
		return self::$instance;
	}

	public static function addNewDevice($deviceID) {
		self::$addNewDevice -> bind_param("s", $deviceID);
		return (int)self::$addNewDevice -> execute();
	}

	public static function isDeviceIn($deviceID) {
		self::$isDeviceIn -> bind_param("s", $deviceID);
		self::$isDeviceIn -> execute();
		self::$isDeviceIn -> bind_result($count);
		while(self::$isDeviceIn -> fetch()){
			$count = $count;
		}
		return $count;
	}

	public static function insert($deviceID, $person) {
		self::$insert -> bind_param("ss", $deviceID,$person);
		return (int)self::$insert -> execute();
	}

	public static function delete($deviceID, $person) {
		self::$delete -> bind_param("ss", $deviceID,$person);
		return (int)self::$delete -> execute();
	}

	public static function clear($deviceID) {
		self::$clear -> bind_param("s", $deviceID);
		return (int)self::$clear ->execute();
	}

	public static function cleargal($deviceID,$person) {
		self::$cleargal -> bind_param("ss", $deviceID,$person);
		return (int)self::$cleargal ->execute();
	}

	public static function add_photo($title, $deviceID, $person, $data) {
		self::$add_photo -> bind_param("ssss", $title, $deviceID, $person, $data);
		return (int)self::$add_photo ->execute();
	}

	public static function remove_photo($title, $deviceID, $person) {
		self::$remove_photo -> bind_param("s",$title);
		return (int)self::$remove_photo ->execute();
	}

	public static function fetch_person($deviceID) {
		$result = array();
		try{
			self::$fetch_person -> bind_param("s", $deviceID);
			self::$fetch_person -> execute();
			self::$fetch_person -> bind_result($person);
			while (self::$fetch_person->fetch()) {
				array_push($result, $person);
			}
		} catch(Exception $e){
		}
		return $result;
	}

	public static function fetch_data($deviceID,$person) {
		$result = array();
		try{
			self::$fetch_data -> bind_param("ss", $deviceID,$person);
			self::$fetch_data -> execute();
			self::$fetch_data -> bind_result($data);
			while (self::$fetch_data->fetch()) {
				array_push($result, json_decode($data,true));
			}
		} catch(Exception $e){
		}
		return $result;
	}

	function __destruct() {
		self::$addNewDevice -> close();
		self::$isDeviceIn -> close();
		self::$insert -> close();
		self::$delete -> close();
		self::$clear -> close();
		self::$cleargal -> close();
		self::$add_photo -> close();
		self::$remove_photo -> close();
		self::$fetch_person -> close();
		self::$fetch_data -> close();
		self::$conn -> close();
	}
}

?>