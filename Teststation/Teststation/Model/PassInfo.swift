import Foundation

enum TestResultType: String, CaseIterable, Codable {
    case UNKNOWN = "UNKNOWN"
    case POSITIVE = "POSITIVE"
    case NEGATIVE = "NEGATIVE"
    
    static func fromCertificateStatus(status:CertificateStatus) -> TestResultType {
        switch status {
        case .negative:
            return .NEGATIVE
        case .positive:
            return .POSITIVE
        case .unknown:
            return .UNKNOWN
        }
    }
}

struct PassInfo: Decodable {
    var serialNumber: String
    var userId: String
    var testResult: TestResultType
    var created: Date
    var validUntil: Date
}
