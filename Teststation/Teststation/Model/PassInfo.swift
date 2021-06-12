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
    case ISSUED = "ISSUED"
    case EXPIRED = "EXPIRED"
    case REVOKED = "REVOKED"
    case REJECTED = "REJECTED"
    case DELETED = "DELETED"
    case UNKNOWN = "UNKNOWN"
    
    static func fromCertificateIssueStatus(issueStatus: CertificateIssueStatus) -> IssueStatus {
        switch issueStatus {
        case .created:
            return .CREATED
        case .issued:
            return .ISSUED
        case .rejected:
            return .REJECTED
        case .revoked:
            return .REVOKED
        case .expired:
            return .EXPIRED
        case .deleted:
            return .DELETED
        case .unknown:
            return .UNKNOWN
        }
    }
    
    func toCertificateIssueStatus() -> CertificateIssueStatus {
        switch self {
        case .CREATED:
            return .created
        case .ISSUED:
            return .issued
        case .REJECTED:
            return .rejected
        case .REVOKED:
            return .revoked
        case .EXPIRED:
            return .expired
        case .DELETED:
            return .deleted
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
    
    init(firstName:String, lastName:String, phoneNo:String, email:String) {
        self.firstName = firstName
        self.lastName = lastName
        self.phoneNo = phoneNo
        self.email = email
    }
}


struct GeoPosition: Decodable {
    var latitude: String
    var longitude: String
}

struct Address: Decodable{
    var city: String
    var zipcode: String
    var street: String?
    var hno: String?
    var position: GeoPosition?
    
    init(city:String, zipcode:String, street:String, hno:String) {
        self.city = city
        self.zipcode = zipcode
        self.street = street
        self.hno = hno
    }
}


struct Teststation: Decodable {
    var id: String
    var name: String
    var address: Address
    var created: Date = Date()
    
    init(id:String, name:String, address:Address) {
        self.id = id
        self.name = name
        self.address = address
    }
}


struct Tester: Decodable {
    var id: String
    var teststationId: String
    var person: Person
    var created: Date = Date()
    
    init(id:String, teststationId:String, person:Person) {
        self.id = id
        self.teststationId = teststationId
        self.person = person
    }
}


struct PassInfo: Decodable {
    var serialNumber: String
    var passInfoId: String
    var imageId: String
    var passId: String?
    var teststationId: String
    var testerId: String
    var issueStatus: IssueStatus
    var version: Int
    var updated: Date
    var validUntil: Date?
}


struct CreatePassResponse: Decodable {
    var serialNumber: String
}
