import Foundation

struct CreatePassRequest: Codable {
    let firstName: String
    let lastName: String
    let phoneNo: String
    let email: String?
    let teststationId: String
    let testerId: String
    let testResult: TestResultType
    let testType: TestType
    let photo: Data
    
    func toMultipart(boundary: String) -> Data {
        let result = NSMutableData()
        
        result.appendFormField(named: "firstName", value: firstName, using: boundary);
        result.appendFormField(named: "lastName", value: lastName, using: boundary);
        result.appendFormField(named: "phoneNo", value: phoneNo, using: boundary);
        result.appendFormField(named: "email", value: email, using: boundary);
        result.appendFormField(named: "teststationId", value: teststationId, using: boundary);
        result.appendFormField(named: "testerId", value: testerId, using: boundary);
        result.appendFormField(named: "testResult", value: testResult.rawValue, using: boundary);
        result.appendFormField(named: "testType", value: testType.rawValue, using: boundary);
        
        result.appendFileData(fieldName: "image", fileName: "\(boundary).png",  mimeType: "image/png", fileData: photo, using: boundary)
        
        result.appendEndMarker(boundary: boundary)
        
        return result as Data
    }
}
