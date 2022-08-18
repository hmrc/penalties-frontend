
# Penalties Frontend

Service that allows VAT traders/agents to view their/their clients VAT penalties and appeals. 

It also allows users to appeal the penalties, view how the penalties were calculated and to see when their penalties will be removed from their account.

This service does not use Mongo.

## Running

This application runs on port 9180.

You can use the `./run.sh` to run the service.

The user must have an authenticated session and be enrolled in MTD VAT to access most pages of this service.

The service manager configuration name for this service is: `PENALTIES_FRONTEND`

This service is dependent on other services, all dependent services can be started with
`sm --start PENALTIES_ALL` (this will also start the penalties frontend microservice so you may need to stop it via `sm --stop PENALTIES_FRONTEND`).

## Testing

This service can be tested with SBT via `sbt test it:test`

To run coverage and scalastyle, please run: `sbt clean scalastyle coverage test it:test coverageReport`
 
## License

This code is open source software licensed under the [Apache 2.0 License]("http://www.apache.org/licenses/LICENSE-2.0.html").
