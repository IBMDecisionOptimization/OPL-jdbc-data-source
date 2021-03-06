// --------------------------------------------------------------------------
// Licensed Materials - Property of IBM
//
// 5725-A06 5725-A29 5724-Y48 5724-Y49 5724-Y54 5724-Y55
// Copyright IBM Corporation 1998, 2013. All Rights Reserved.
//
// Note to U.S. Government Users Restricted Rights:
// Use, duplication or disclosure restricted by GSA ADP Schedule
// Contract with IBM Corp.
// --------------------------------------------------------------------------


tuple gasType {
   string name;
   float demand;
   float price;
   float octane;
   float lead;
}

tuple oilType {
   string name;
   float capacity;
   float price;
   float octane;
   float lead;
}
{gasType} GasData = { <"Super", 3000, 70, 10, 1>,
                      <"Regular", 2000, 60, 8, 2>,
                      <"Diesel", 1000, 50, 6, 1>};

{oilType} OilData = { <"Crude1", 5000, 45, 12, 0.5>, 
                      <"Crude2", 5000, 35, 6, 2>,
				      <"Crude3", 5000, 25, 8, 3>};


execute {
}
