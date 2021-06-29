FROM fsouza/fake-gcs-server:latest
# "-port=80"
COPY data /data
ENTRYPOINT ["/bin/fake-gcs-server", "-scheme=http", "-backend=filesystem", "-data=/data"]