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
    
    func toCertificateStatus() -> CertificateStatus {
        switch self {
        case .NEGATIVE:
            return .negative
        case .POSITIVE:
            return .positive
        case .UNKNOWN:
            return .unknown
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

enum IssueStatus: String, CaseIterable, Codable {
    case CREATED = "CREATED"
    case SIGNED = "SIGNED"
    case EXPIRED = "EXPIRED"
    case REVOKED = "REVOKED"
    case REFUSED = "REFUSED"
    case PENDING = "PENDING"
    case FAILED = "FAILED"
    case UNKNOWN = "UNKNOWN"
    
    static func fromCertificateIssueStatus(issueStatus: CertificateIssueStatus) -> IssueStatus {
        switch issueStatus {
        case .created:
            return .CREATED
        case .pending:
            return .PENDING
        case .signed:
            return .SIGNED
        case .refused:
            return .REFUSED
        case .failed:
            return .FAILED
        case .revoked:
            return .REVOKED
        case .expired:
            return .EXPIRED
        case .unknown:
            return .UNKNOWN
        }
    }
    
    func toCertificateIssueStatus() -> CertificateIssueStatus {
        switch self {
        case .CREATED:
            return .created
        case .PENDING:
            return .pending
        case .SIGNED:
            return .signed
        case .REFUSED:
            return .refused
        case .FAILED:
            return .failed
        case .REVOKED:
            return .revoked
        case .EXPIRED:
            return .expired
        case .UNKNOWN:
            return .unknown
        }
    }
}


struct Person: Decodable{
    var firstName: String
    var lastName: String
    var phoneNo: String?
    var email: String?
}

struct PassInfo: Decodable {
    var serialNumber: String
    var person: Person
    var imageId: String
    var teststationId: String
    var testerId: String
    var authToken: String
    var testResult: TestResultType
    var testType: TestType
    var issueStatus: IssueStatus
    var created: Date
    var modified: Date?
    var passId: String?
    var validUntil: Date?
    var version: Int
}
