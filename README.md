docker-compose using nginx.conf for sr iiif image server
===================================================================

Tutorial
---------
1) ensure docker desktop is running

2) git clone this repo

3) ```docker network create proxy-external``` # check if this is needed and specify network IP according to Docker's conventions
(see https://docs.docker.com/compose/compose-file/#networks)
(seems unncessary for testing, perhaps useful when serving to develop? see https://docs.docker.com/compose/compose-file/#external)

4) let's get in the directory and run -detached (-d)<br/>
```cd sr_dkr_iiif-server_updated```<br/>
```docker-compose up -d```<br/>

6) open a browser and go to<br/>
http://localhost:8182

NOTES:
------
TK
