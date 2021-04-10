import Foundation

struct CreatePassRequest: Codable {
    let userId: String
    let testResult: TestResultType
}
