import Foundation

enum TestResultType: String, CaseIterable, Decodable {
    case UNKNOWN = "UNKNOWN"
    case POSITIVE = "POSITIVE"
    case NEGATIVE = "NEGATIVE"
}

struct PassInfo: Decodable {
    var serialNumber: String
    var userId: String
    var testResult: TestResultType
    var created: Date
    var validUntil: Date
}
