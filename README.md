
# claim-child-benefit-admin-frontend
The claim admin frontend is a tool used to resend submissions to SDES. 

Currently when a claim is submitted for a user it is required to also be looked at by a caseworker. This PDF is generated and sent via SDES and object store. 

1. Upload file to object-store
2. Send a notification to SDES with the path to object-store so it can pick up the file
3. Get callback from SDES when the file has been successfully processed.

The admin service aim is to assist with difficulties with SDES where a submission is stuck in step 2 and step 3 has not been accomplished. 

<img width="1051" alt="Screenshot 2023-12-13 at 12 58 48" src="https://github.com/hmrc/claim-child-benefit-admin-frontend/assets/59606793/350eac8c-c99c-47bd-a45b-d84fa42c0b83">

Therefore you can re-notify SDES of a submission by resending a file. See image below
<img width="1131" alt="Screenshot 2023-12-13 at 13 00 12" src="https://github.com/hmrc/claim-child-benefit-admin-frontend/assets/59606793/091efb03-7abe-4323-ac8e-972842503791">


### License

This code is open source software licensed under the [Apache 2.0 License]("http://www.apache.org/licenses/LICENSE-2.0.html").
