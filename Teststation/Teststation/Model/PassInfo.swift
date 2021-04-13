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


enum TestType: String, CaseIterable, Codable {
    case RAPIDTEST = "RAPIDTEST"
    case PCRTEST = "PCRTEST"
    case VACCINATION = "VACCINATION"
    case UNKNOWN = "UNKNOWN"
    
    static func fromCertificateType(type: CertificateType) -> TestType {
        switch type {
        case .pcrtest:
            return .PCRTEST
        case .rapidtest:
            return .RAPIDTEST
        case .vaccination:
            return .VACCINATION
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
