## **Account Helper Tool**

  This Java-based application is designed to streamline business data validation and address processing using the Google Places API. It processes data from an input .xls file, validates it with API results, and exports a processed .xls file. It was created primarily to reduce manual address verification from a company database system that has .xls file export capabilities. 

## **Tech Stack**

Language: Java  
Libraries: Apache POI, org.json  
API: Google Places API  


## **Deployment**

  1. Ensure Java and Maven are installed. Verify environment variables JAVA_HOME and MAVEN_HOME are set up.
  2. Clone the repository: git clone https://github.com/Jerem-Dough/Account-Helper-Tool.git
  3. Navigate to the proper directory: cd Account-Helper-Tool
  4. Install dependencies: mvn clean install
  5. Run the program: mvn exec:java -Dexec.mainClass="nopk.App"
  6. The program takes .xls files of the following format:
      - Column G: Name - The business name.
      - Column K: Address 1 - The primary address for the business.
      - Column L: Address 2 - An alternative address for the business.
      - Column M: City - The city where the business is located.
      - Column N: State - The state where the business is located. (Ex: CO, CA, WA)
      - Column O: Zip Code - The postal code for the business.
