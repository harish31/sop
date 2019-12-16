 drop table order_status if exists;
CREATE TABLE `order_status` (
  `order_status_id` smallint(6) NOT NULL,
  `order_status_code` varchar(30) NOT NULL,
  `description` varchar(50) DEFAULT NULL,
  PRIMARY KEY (`order_status_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

drop table order_type if exists;
CREATE TABLE `order_type` (
  `order_type_id` smallint(6) NOT NULL,
  `order_type_code` varchar(10) NOT NULL,
  `description` varchar(50) DEFAULT NULL,
  PRIMARY KEY (`order_type_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

drop table order_line_status if exists;
CREATE TABLE `order_line_status` (
  `order_line_status_id` smallint(6) NOT NULL,
  `order_line_status_code` varchar(30) NOT NULL,
  `description` varchar(50) DEFAULT NULL,
  PRIMARY KEY (`order_line_status_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

drop table error_code if exists;
CREATE TABLE `error_code` (
  `error_code` int(11) NOT NULL,
  `error_label` varchar(50) NOT NULL,
  `severity` varchar(10) NOT NULL,
  PRIMARY KEY (`error_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

drop table enrich_status if exists;
CREATE TABLE `enrich_status` (
  `enrich_status_id` int(6) NOT NULL,
  `enrich_label` varchar(30) DEFAULT NULL,
  `description` varchar(255) DEFAULT NULL,
  `enrich_status_code` varchar(30) DEFAULT NULL,
  PRIMARY KEY (`enrich_status_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

INSERT INTO `order_status` (`order_status_id`,`order_status_code`,`description`) VALUES (201,'NEW','New');
INSERT INTO `order_status` (`order_status_id`,`order_status_code`,`description`) VALUES (202,'ON_HOLD','Hold');
INSERT INTO `order_status` (`order_status_id`,`order_status_code`,`description`) VALUES (203,'ACTIVE','Active');
INSERT INTO `order_status` (`order_status_id`,`order_status_code`,`description`) VALUES (204,'INACTIVE','Inactive');
INSERT INTO `order_status` (`order_status_id`,`order_status_code`,`description`) VALUES (205,'REL_FUL','Released for fulfillement');
INSERT INTO `order_status` (`order_status_id`,`order_status_code`,`description`) VALUES (206,'REL_ROUT','Released to routing');
INSERT INTO `order_status` (`order_status_id`,`order_status_code`,`description`) VALUES (207,'CANCELLED','Cancelled');
INSERT INTO `order_status` (`order_status_id`,`order_status_code`,`description`) VALUES (208,'INVALID','Invalid');
INSERT INTO `order_status` (`order_status_id`,`order_status_code`,`description`) VALUES (209,'SUCCESS','SUCCESS');
INSERT INTO `order_status` (`order_status_id`,`order_status_code`,`description`) VALUES (210,'ENRICHMENT_IN_PROGRESS','ENRICHMENT_IN_PROGRESS');

INSERT INTO `order_type` (`order_type_id`,`order_type_code`,`description`) VALUES (101,'REGULAR','Regular');
INSERT INTO `order_type` (`order_type_id`,`order_type_code`,`description`) VALUES (102,'RUSH','Rush');
INSERT INTO `order_type` (`order_type_id`,`order_type_code`,`description`) VALUES (103,'FIRM','Firm');
INSERT INTO `order_type` (`order_type_id`,`order_type_code`,`description`) VALUES (104,'STANDING','Standing');
INSERT INTO `order_type` (`order_type_id`,`order_type_code`,`description`) VALUES (105,'TRANSFER','Transfer');
INSERT INTO `order_type` (`order_type_id`,`order_type_code`,`description`) VALUES (106,'SYSTEM','System');

INSERT INTO `order_line_status` (`order_line_status_id`,`order_line_status_code`,`description`) VALUES (251,'ACTIVE','Active');
INSERT INTO `order_line_status` (`order_line_status_id`,`order_line_status_code`,`description`) VALUES (252,'CANCELLED','Cancelled');
INSERT INTO `order_line_status` (`order_line_status_id`,`order_line_status_code`,`description`) VALUES (253,'REPLACED','Replaced');
INSERT INTO `order_line_status` (`order_line_status_id`,`order_line_status_code`,`description`) VALUES (254,'SUBSTITUTED','Substituted');
INSERT INTO `order_line_status` (`order_line_status_id`,`order_line_status_code`,`description`) VALUES (257,'ON_HOLD','Hold');

INSERT INTO `enrich_status` (`enrich_status_id`,`enrich_label`,`description`,`enrich_status_code`) VALUES (100,'Success','When any capability completes its task successfully','SUCCESS');
INSERT INTO `enrich_status` (`enrich_status_id`,`enrich_label`,`description`,`enrich_status_code`) VALUES (101,'Failed','Completed successfully its tasks but resulted in validation failure','FAILED');
INSERT INTO `enrich_status` (`enrich_status_id`,`enrich_label`,`description`,`enrich_status_code`) VALUES (102,'Error','System Exception in capability','ERROR');
INSERT INTO `enrich_status` (`enrich_status_id`,`enrich_label`,`description`,`enrich_status_code`) VALUES (103,'Pending for Execution','Pending for Execution','PENDING');
INSERT INTO `enrich_status` (`enrich_status_id`,`enrich_label`,`description`,`enrich_status_code`) VALUES (104,'In progress','This is used when capability is in running in ASYNCH mode like auto-Allocation waiting for Aynch Response which will directly update order_enrichment_tracker to SUCCESS / FAILED / ERROR ','IN_PROGRESS');
INSERT INTO `enrich_status` (`enrich_status_id`,`enrich_label`,`description`,`enrich_status_code`) VALUES (105,'Skipped timeout','System was not able to Enrich in right time and delayed mainly by recovery job','SKIPPED_TIMEOUT');
INSERT INTO `enrich_status` (`enrich_status_id`,`enrich_label`,`description`,`enrich_status_code`) VALUES (106,'Enrich retry exceeded','System was not able to Enrich after configurable retry count','ENRICH_RETRY_EXCEEDED');
INSERT INTO `enrich_status` (`enrich_status_id`,`enrich_label`,`description`,`enrich_status_code`) VALUES (107,'Skipped by rule','System was not able to Enrich after configurable retry count','SKIPPED_BY_RULE');
INSERT INTO `enrich_status` (`enrich_status_id`,`enrich_label`,`description`,`enrich_status_code`) VALUES (108,'Skipped because re-enrich','Skipped because of RE-ENRICH','SKIPPED_BECAUSE_REENRICH');
INSERT INTO `enrich_status` (`enrich_status_id`,`enrich_label`,`description`,`enrich_status_code`) VALUES (109,'Skipped because duplicate','Skipped because of duplicates','SKIPPED_BECAUSE_DUPLICATE');
INSERT INTO `enrich_status` (`enrich_status_id`,`enrich_label`,`description`,`enrich_status_code`) VALUES (110,'Skipped because no valid lines','There are no lines to be enriched ','SKIPPED_BECAUSE_NO_VALID_LINES');
INSERT INTO `enrich_status` (`enrich_status_id`,`enrich_label`,`description`,`enrich_status_code`) VALUES (111,'Skipped No BOH','Skipped because BOH not available.','SKIPPED_NO_BOH');


INSERT INTO `capability` (`capability_id`,`capability_label`,`capability_interface`,`capability_type`,`timeout_duration`,`description`) VALUES (1,'PE','productCommandCapability','PE',2,'');
INSERT INTO `capability` (`capability_id`,`capability_label`,`capability_interface`,`capability_type`,`timeout_duration`,`description`) VALUES (2,'PSE','productSupplierCommandCapability','SE',2,'');
INSERT INTO `capability` (`capability_id`,`capability_label`,`capability_interface`,`capability_type`,`timeout_duration`,`description`) VALUES (3,'UOM','uomCommandCapability','UO',2,'');
INSERT INTO `capability` (`capability_id`,`capability_label`,`capability_interface`,`capability_type`,`timeout_duration`,`description`) VALUES (4,'PR','palletRoundingCommandCapability','PR',2,'');
INSERT INTO `capability` (`capability_id`,`capability_label`,`capability_interface`,`capability_type`,`timeout_duration`,`description`) VALUES (5,'PQ','palletQuantityCommandCapability','PQ',2,'');
INSERT INTO `capability` (`capability_id`,`capability_label`,`capability_interface`,`capability_type`,`timeout_duration`,`description`) VALUES (6,'CR','caseRoundingCommandCapability','CR',2,'');
INSERT INTO `capability` (`capability_id`,`capability_label`,`capability_interface`,`capability_type`,`timeout_duration`,`description`) VALUES (7,'CQ','casePackQtyCommandCapability','CQ',2,'');
INSERT INTO `capability` (`capability_id`,`capability_label`,`capability_interface`,`capability_type`,`timeout_duration`,`description`) VALUES (8,'FN','finalizerCapability','FN',2,'');
INSERT INTO `capability` (`capability_id`,`capability_label`,`capability_interface`,`capability_type`,`timeout_duration`,`description`) VALUES (9,'AA','orderAutoAllocationCommand','AA',2,'');

INSERT INTO `enrich_sequence` (`enrich_sequence_id`,`order_type_id`,`supp_type_id`,`cust_type_id`,`seq_num`,`capability_id`) VALUES (1,102,301,350,2,1);
INSERT INTO `enrich_sequence` (`enrich_sequence_id`,`order_type_id`,`supp_type_id`,`cust_type_id`,`seq_num`,`capability_id`) VALUES (2,102,301,350,3,2);
INSERT INTO `enrich_sequence` (`enrich_sequence_id`,`order_type_id`,`supp_type_id`,`cust_type_id`,`seq_num`,`capability_id`) VALUES (3,102,301,350,4,3);
INSERT INTO `enrich_sequence` (`enrich_sequence_id`,`order_type_id`,`supp_type_id`,`cust_type_id`,`seq_num`,`capability_id`) VALUES (4,102,301,350,5,4);
INSERT INTO `enrich_sequence` (`enrich_sequence_id`,`order_type_id`,`supp_type_id`,`cust_type_id`,`seq_num`,`capability_id`) VALUES (6,102,301,350,6,5);
INSERT INTO `enrich_sequence` (`enrich_sequence_id`,`order_type_id`,`supp_type_id`,`cust_type_id`,`seq_num`,`capability_id`) VALUES (7,102,301,350,7,6);
INSERT INTO `enrich_sequence` (`enrich_sequence_id`,`order_type_id`,`supp_type_id`,`cust_type_id`,`seq_num`,`capability_id`) VALUES (8,102,301,350,8,7);
INSERT INTO `enrich_sequence` (`enrich_sequence_id`,`order_type_id`,`supp_type_id`,`cust_type_id`,`seq_num`,`capability_id`) VALUES (9,102,301,350,9999,8);
INSERT INTO `enrich_sequence` (`enrich_sequence_id`,`order_type_id`,`supp_type_id`,`cust_type_id`,`seq_num`,`capability_id`) VALUES (10,102,301,350,9,9);

INSERT INTO `error_code` (`error_code`,`error_label`,`severity`) VALUES (1001,'NO_SUPPLIER_FOUND','ERROR');
INSERT INTO `error_code` (`error_code`,`error_label`,`severity`) VALUES (1002,'ORDER_NOT_FOUND','ERROR');
INSERT INTO `error_code` (`error_code`,`error_label`,`severity`) VALUES (1003,'ORDER_LINE_NOT_FOUND','ERROR');
INSERT INTO `error_code` (`error_code`,`error_label`,`severity`) VALUES (1004,'CUSTOMER_NOT_FOUND','ERROR');
INSERT INTO `error_code` (`error_code`,`error_label`,`severity`) VALUES (1005,'SOS_NOT_FOUND','ERROR');
INSERT INTO `error_code` (`error_code`,`error_label`,`severity`) VALUES (1006,'PRODUCT_SUPPLIER_NOT_FOUND','ERROR');
INSERT INTO `error_code` (`error_code`,`error_label`,`severity`) VALUES (1007,'RULE_ALREADY_DEFINED','ERROR');
INSERT INTO `error_code` (`error_code`,`error_label`,`severity`) VALUES (1008,'RULE_NOT_FOUND','ERROR');
INSERT INTO `error_code` (`error_code`,`error_label`,`severity`) VALUES (1009,'INVALID_DIVISION','ERROR');
INSERT INTO `error_code` (`error_code`,`error_label`,`severity`) VALUES (1010,'DUPLICATE_ORDER','ERROR');
INSERT INTO `error_code` (`error_code`,`error_label`,`severity`) VALUES (1011,'INVALID_PRODUCT','ERROR');
INSERT INTO `error_code` (`error_code`,`error_label`,`severity`) VALUES (1012,'UNAUTHORIZED_PRODUCT','ERROR');
INSERT INTO `error_code` (`error_code`,`error_label`,`severity`) VALUES (1013,'INVALID_ITEM_QUANTITY','ERROR');
INSERT INTO `error_code` (`error_code`,`error_label`,`severity`) VALUES (1014,'INVALID_SCHEDULED_DATE','ERROR');
INSERT INTO `error_code` (`error_code`,`error_label`,`severity`) VALUES (1015,'SEQUENCER_FINALIZER_ERROR','ERROR');
INSERT INTO `error_code` (`error_code`,`error_label`,`severity`) VALUES (1016,'USER_NOT_FOUND','ERROR');
INSERT INTO `error_code` (`error_code`,`error_label`,`severity`) VALUES (1017,'GROUP_NOT_FOUND','ERROR');
INSERT INTO `error_code` (`error_code`,`error_label`,`severity`) VALUES (1018,'PRODUCT_SUBSTITUTION_NOT_FOUND','ERROR');
INSERT INTO `error_code` (`error_code`,`error_label`,`severity`) VALUES (1019,'SKIPPED_BECAUSE_REENRICH','INFO');
INSERT INTO `error_code` (`error_code`,`error_label`,`severity`) VALUES (1020,'SKIPPED_BY_RULE','INFO');
INSERT INTO `error_code` (`error_code`,`error_label`,`severity`) VALUES (1021,'SKIPPED_BECAUSE_NO_VALID_LINES','INFO');
INSERT INTO `error_code` (`error_code`,`error_label`,`severity`) VALUES (1022,'AUTO_ALLOCATION_POST_ERROR','ERROR');
INSERT INTO `error_code` (`error_code`,`error_label`,`severity`) VALUES (1023,'ORDER_ENRICHMENT_FAILED','ERROR');
INSERT INTO `error_code` (`error_code`,`error_label`,`severity`) VALUES (1024,'INVALID_CAPABILITY_BEAN_RESULT','ERROR');
INSERT INTO `error_code` (`error_code`,`error_label`,`severity`) VALUES (1025,'PRODUCT_NOT_FOUND','ERROR');
INSERT INTO `error_code` (`error_code`,`error_label`,`severity`) VALUES (1026,'SERVICE_ERROR','ERROR');
INSERT INTO `error_code` (`error_code`,`error_label`,`severity`) VALUES (1027,'RETAILUPC_NOT_FOUND','ERROR');
INSERT INTO `error_code` (`error_code`,`error_label`,`severity`) VALUES (1028,'RETAIL_SECTIONCODE_NOT_FOUND','ERROR');
INSERT INTO `error_code` (`error_code`,`error_label`,`severity`) VALUES (1029,'CASEUPC_NOT_FOUND','ERROR');
INSERT INTO `error_code` (`error_code`,`error_label`,`severity`) VALUES (1030,'PRODUCT_DESCRIPTION_NOT_FOUND','ERROR');
INSERT INTO `error_code` (`error_code`,`error_label`,`severity`) VALUES (1031,'SUPPLIER_CUBE_NOT_FOUND','ERROR');
INSERT INTO `error_code` (`error_code`,`error_label`,`severity`) VALUES (1032,'SHIP_UNIT_PACK_QTY_NOT_FOUND','ERROR');
INSERT INTO `error_code` (`error_code`,`error_label`,`severity`) VALUES (1033,'SHIP_UNIT_PACK_TYPE_CD_NOT_FOUND','ERROR');
INSERT INTO `error_code` (`error_code`,`error_label`,`severity`) VALUES (1034,'PALLET_LABEL_NOT_FOUND','ERROR');
INSERT INTO `error_code` (`error_code`,`error_label`,`severity`) VALUES (1035,'PALLET_HEIGHT_NOT_FOUND','ERROR');
INSERT INTO `error_code` (`error_code`,`error_label`,`severity`) VALUES (1036,'CONV_FACTOR_NOT_FOUND','ERROR');
INSERT INTO `error_code` (`error_code`,`error_label`,`severity`) VALUES (1037,'BRANCH_ITEM_CD_NOT_FOUND','ERROR');
INSERT INTO `error_code` (`error_code`,`error_label`,`severity`) VALUES (1038,'WHSE_ITEM_CD_NOT_FOUND','ERROR');
INSERT INTO `error_code` (`error_code`,`error_label`,`severity`) VALUES (1039,'UOM_NOT_FOUND','ERROR');
INSERT INTO `error_code` (`error_code`,`error_label`,`severity`) VALUES (1040,'ORDERED_ITEM_QTY_NOT_FOUND','ERROR');
INSERT INTO `error_code` (`error_code`,`error_label`,`severity`) VALUES (1041,'CASE_QTY_NOT_FOUND','ERROR');
INSERT INTO `error_code` (`error_code`,`error_label`,`severity`) VALUES (1042,'PALLET_QTY_NOT_FOUND','ERROR');
INSERT INTO `error_code` (`error_code`,`error_label`,`severity`) VALUES (1043,'SKIPPED_NO_BOH','INFO');
INSERT INTO `error_code` (`error_code`,`error_label`,`severity`) VALUES (1044,'SKIPPED_ALLOCATION_UP','INFO');

drop table order_error if exists;
CREATE TABLE `order_error` (
  `order_error_id` int(11) NOT NULL AUTO_INCREMENT,
  `order_id` int(11) NULL,
  `line_nbr` int(11) DEFAULT '0',
  `error_code` int(11) NOT NULL,
  `current_value` varchar(30) DEFAULT NULL,
  `create_ts` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `created_by` varchar(10) NOT NULL,
  PRIMARY KEY (`order_error_id`),
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

drop table message if exists;
CREATE TABLE `message` (
  `id` bigint(20) NOT NULL,
  `component_id` int(11) DEFAULT NULL,
  `created_ts` datetime DEFAULT NULL,
  `endpoint` varchar(255) DEFAULT NULL,
  `message` varchar(255) DEFAULT NULL,
  `retry_count` int(11) DEFAULT NULL,
  `updated_ts` datetime DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
