It is part of Master Thesis "Adaptive Video Techniques for Informal Learning Support in Workplace Environments" at Chair of Computer Science 5, RWTH-Aachen.

It is responsible to keep track of information related to different video segments. The weight of a segment, total number of views, view duration for a segment by a particular user, etc. This service is requested by the adapter service to get the weight and analytics for a particular segment. This service manages two tables, one representing the weight and domain of a video segment, and the other table keeps record of how much was a certain video segment viewed.

Compile using ant, with "ant all". Then run bin/start_network.bat for windows or bin/start_network.sh for linux.