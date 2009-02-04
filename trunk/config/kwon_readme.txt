==========
README.TXT
==========

This folder contains files related to Aurora GIS Importer


East Bay Network
================
* TANA (Tele Atlas North America) GIS data for CA, Alameda, Street Layer 
(For each *.shp file, *.dbf and *.shx files are also present)

caalamst.shp : GIS data for the whole area

caalamst_tiny.shp : GIS data for a small part of caalamst.shp
  cropped using OpenJUMP

caalamst_hwy.shp : GIS data for all highways (ACC = 1,2,3) 
  filtered using within GIS Importer ("road type filter" function)

* Output:

caalamst_tiny.xml : tiny Bay Area network

caalamst_hwy.xml : CA Alemda Bay Area 

caalamst_small.xml.gz : "small" Bay Area network; it is 2.3MB
compressed and cause error in aurora (heap space error)


SANDAG Network
==============

* Raw (and somewhat processed) GIS data

hwycov03_ARC.shp : GIS data for SANDAG network

hwycov03_geofiltered.shp :  cropped

hwycov03_typefiltered.shp :  filtered by road types (1, 2, 3, 4, 8, 9)

hwycov03_simplified.shp : edge-simplified

* Output:

hwycov03_simplified.xml : output aurora xml



MISC
====
xmlformat.pl : xml formatter; 
 e.g. 
 % perl xmlformat.pl caalamst_hwy.xml > clean_caalamst_hwy.xml

Transp6x_04.pdf : description of TANA data; a must read for dealing
 with TANA data

