FROM osrm/osrm-backend:latest

WORKDIR /data

EXPOSE 5000

CMD ["osrm-routed", "--algorithm", "mld", "/data/vietnam-latest.osrm"]