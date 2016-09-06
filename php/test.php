<?php
require_once "ModelMail.php";

    echo 'start'.PHP_EOL;
    ModelMail::send_mail('honmura.and@gmail.com','test','testc');
    echo 'end'.PHP_EOL;

    
