# Use an official Ubuntu base image
FROM ubuntu:latest

# Install curl
RUN apt-get update && apt-get install -y curl

WORKDIR /

# Copy the shell script into the container
COPY loop_request.sh ./loop_request.sh

# Make the shell script executable
RUN chmod +x ./loop_request.sh

# Set the entrypoint to the shell script
ENTRYPOINT ["bash", "-c", "./loop_request.sh"]
#CMD ["ls ./loop_request.sh"]
