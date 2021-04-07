import SwiftUI
import Combine
import CoreData

class TeststationEngine {
    
    static let shared = TeststationEngine()
    
    private let moc :NSManagedObjectContext
    
    init() {
        print("TeststationEngine()")
        
        moc = (UIApplication.shared.delegate as!AppDelegate).persistentContainer.newBackgroundContext()
        moc.automaticallyMergesChangesFromParent = true
        //moc.mergePolicy = NSMergeByPropertyStoreTrumpMergePolicy
    }
    
    func startIssueOfCertificate(mcr: ModifyCertificateResponse,
                                 onChange: @escaping (CertificateIssueStatus)->Void) {
        moc.perform {
            guard let cert = try? self.moc.getCertificate(objectID: mcr.cert.objectID) else {
                return
            }
            
            cert.updateStatus(status: mcr.selectedStatus)
            self.moc.saveContext()
            
            print("TeststationEngine.startIssueOfCertificate(\(cert.id!))")
            
            sleep(4)
            
            cert.updateIssueStatus(issueStatus: CertificateIssueStatus.pending)
            self.moc.saveContext()
            
            onChange(CertificateIssueStatus.pending)
            
            print("TeststationEngine.startIssueOfCertificate(\(cert.id!)) issueStatus=\(cert.issuestatus)")
            
            sleep(4)
            
            cert.updateIssueStatus(issueStatus: CertificateIssueStatus.signed)
            self.moc.saveContext()
            print("TeststationEngine.startIssueOfCertificate(\(cert.id!)) issueStatus=\(cert.issuestatus)")
            
            onChange(CertificateIssueStatus.signed)
            
        }
        
    }
}
