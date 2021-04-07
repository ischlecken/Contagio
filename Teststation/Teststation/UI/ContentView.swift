import SwiftUI
import CoreData

struct ContentView: View {
    @Environment(\.managedObjectContext) var managedObjectContext
    @FetchRequest(
        entity: Certificate.entity(),
        sortDescriptors: [NSSortDescriptor(keyPath: \Certificate.createts, ascending: false)]
    ) var certificates: FetchedResults<Certificate>
    
    @State var issueCertificate = false
    @State var isPresented = false
    @State var certificatePhotos = [String : UIImage] ()
    
    var body: some View {
        NavigationView {
            List {
                if( issueCertificate ) {
                    ProgressView("Issue Certificate")
                }
                ForEach(certificates) { cert in
                    NavigationLink( destination: ModifyCertificate(
                        certificate:cert,
                        certifcatePhoto: certificatePhotos[cert.id!] ?? UIImage(named:"passdefaultimg")!,
                        selectedStatus: Int(cert.status) ) { mcr in
                        
                        if( mcr.shouldDelete ) {
                            managedObjectContext.deleteCertificate(certificate: mcr.cert)
                        }
                        else {
                            issueCertificate = true
                            
                            TeststationEngine.shared.startIssueOfCertificate(mcr: mcr) { issueStatus in
                                print("ContentView() issueStatus=\(issueStatus)")
                                
                                if( issueStatus.isFinished() ) {
                                    DispatchQueue.main.async {
                                        issueCertificate = false
                                        
                                        do {
                                            let c0 = try managedObjectContext.getCertificate(objectID: mcr.cert.objectID)
                                            
                                            print("c.status=\(String(describing: c0?.status)) c.issuestatus=\(String(describing: c0?.issuestatus))")
                                            
                                            c0?.modifyts = Date()
                                        } catch {
                                        }
                                        
                                        managedObjectContext.saveContext()
                                    }
                                }
                            }
                        }
                    }) { CertificateRow(certificate: cert, photo: self.certificatePhotos[cert.id!]) }
                    .listRowInsets(EdgeInsets(top: 10, leading: 10, bottom: 10, trailing: 16))
                }
                .onDelete { offsets in
                    offsets.forEach { index in
                        let certificate = certificates[index]
                        
                        managedObjectContext.deleteCertificate(certificate: certificate)
                    }
                }
            }
            .listStyle(GroupedListStyle())
            .onAppear {
                (UIApplication.shared.delegate as!AppDelegate).persistentContainer.performBackgroundTask { context in
                    let newCertPhotos = context.loadPictures(certificates: certificates)
                    
                    DispatchQueue.main.async {
                        self.certificatePhotos = newCertPhotos
                    }
                }
            }
            .sheet(isPresented: $isPresented) {
                AddCertificate { acr in
                    let cert = managedObjectContext.addCertificate(acr: acr)
                    
                    managedObjectContext.saveContext()
                    
                    certificatePhotos[cert.id!] = acr.photo
                    isPresented = false
                }
            }
            .navigationTitle("certificatelist_title")
            .navigationBarItems(
                trailing: Button(action: { isPresented.toggle() }) {Image(systemName: "plus") }
            )
        }
    }
    
}

struct ContentView_Previews: PreviewProvider {
    
    static var previews: some View {
        let context = (UIApplication.shared.delegate as!AppDelegate).persistentContainer.viewContext
        
        Group {
            ContentView().environment(\.managedObjectContext, context)
        }
    }
}
