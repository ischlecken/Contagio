import UIKit

struct AddCertificateResponse {
    let firstname: String
    let lastname: String
    let phonenumber: String
    let email: String
    let type: CertificateType
    let status: CertificateStatus
    let photo: UIImage
}

struct ModifyCertificateResponse {
    let cert: Certificate
    let selectedStatus: CertificateStatus
    let shouldDelete: Bool
}
