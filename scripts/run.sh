gor --input-raw <your-prod-server> \
    --output-http=<your-staging-server> \
    --middleware "./target/appassembler/bin/gor-tester outputdirname" \
    --output-http-track-response \
    --input-raw-track-response \
    --prettify-http
