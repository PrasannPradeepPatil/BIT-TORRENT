user="aryansingh"
find . -name "*.java" > build.txt
javac -d . @build.txt
ssh $user@lin114-00.cise.ufl.edu 'rm -rf CNT5106C_P2P_Group31'
ssh -t $user@lin114-00.cise.ufl.edu 'mkdir CNT5106C_P2P_Group31'
scp -r Common.cfg PeerInfo.cfg ./edu ./peer_100* $user@lin114-00.cise.ufl.edu:~/CNT5106C_P2P_Group31/
IFS=' '
while read line || [ -n "$line" ] ; do
    read -r -a line_frag <<< "$line"
    ssh -f $user@"${line_frag[1]}" "cd CNT5106C_P2P_Group31;java edu.ufl.cise.process.PeerProcess '${line_frag[0]}' > /dev/null"
    echo "$line process has been started"
done < PeerInfo.cfg
unset IFS

