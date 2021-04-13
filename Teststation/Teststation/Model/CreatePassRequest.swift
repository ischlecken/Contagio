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
}
