Simple app to get repos by given github username

sample input for 
GET localhost:8080/api/githubrepo/repositories

{
    "username": "chrzanekk"
}

as result we receive list of repos with flag fork:false
list contains objects with fields 
- name - repository name
- ownerLogin - owner of branch
- list of branches of repo with branch name and last commit sha

sample curl request
curl \
-X GET \
-H 'Content-Type: application/json' \
-d '{"username":"chrzanekk"}' \
http://localhost:8080/api/githubrepo/repositories

We can improve this app by adding @Async to avoid waiting and dont block threads 
Also test of webClient can be extended for more cases 