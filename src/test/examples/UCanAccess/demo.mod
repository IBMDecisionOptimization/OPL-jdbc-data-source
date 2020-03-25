// ----------------------------------------------------------------------------
// Sample material distributed under Apache 2.0 license.
//
// Copyright IBM Corporation 2019. All Rights Reserved.
// ----------------------------------------------------------------------------


{string} Gasolines = ...;
{string} Oils = ...;
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
{gasType} GasData = ...;
{oilType} OilData = ...;
gasType Gas[Gasolines] = [ g.name : g | g in GasData ];
oilType Oil[Oils] = [ o.name : o | o in OilData ];

float MaxProduction = ...;
float ProdCost = ...;


dvar float+ a[Gasolines];
dvar float+ Blend[Oils][Gasolines];


maximize
  sum( g in Gasolines , o in Oils )
        (Gas[g].price - Oil[o].price - ProdCost) * Blend[o][g]
         - sum( g in Gasolines ) a[g];

subject to {
   
  ctDemand:  forall( g in Gasolines )
               sum( o in Oils ) 
                 Blend[o][g] == Gas[g].demand + 10 * a[g];
   
  ctCapacity:  forall( o in Oils )   
                 sum( g in Gasolines ) 
                   Blend[o][g] <= Oil[o].capacity;

  ctMaxProd:  sum( o in Oils , g in Gasolines ) 
                Blend[o][g] <= MaxProduction;
 
  ctOctane:  forall( g in Gasolines )
               sum( o in Oils ) 
                 (Oil[o].octane - Gas[g].octane) * Blend[o][g] >= 0;
  ctLead:  forall( g in Gasolines )
             sum( o in Oils ) 
               (Oil[o].lead - Gas[g].lead) * Blend[o][g] <= 0;
}
tuple result {
  string oil;
  string gas;
  float blend;
  float a;
}

{result} Result = 
  { <o,g,Blend[o][g],a[g]> | o in Oils, g in Gasolines };
  
execute DISPLAY_RESULT{
  writeln("Result = ",Result)
}
