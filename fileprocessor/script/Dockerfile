# Use an official Ubuntu base image
FROM ubuntu:latest

# Install curl
RUN apt-get update && apt-get install -y curl

# Copy the shell script into the container
COPY loop_request.sh /usr/local/bin/loop_request.sh

# Make the shell script executable
RUN chmod +x /usr/local/bin/loop_request.sh

# Set the entrypoint to the shell script
ENTRYPOINT ["/usr/local/bin/loop_request.sh"]
