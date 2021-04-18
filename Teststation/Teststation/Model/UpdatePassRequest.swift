import Foundation

struct UpdatePassRequest : Encodable{
    var serialNumber: String
    var testResult: TestResultType?
    var validUntil: Date?
}
