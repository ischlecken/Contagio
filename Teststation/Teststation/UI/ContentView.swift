import SwiftUI

struct ContentView: View {
    @Environment(\.managedObjectContext) var managedObjectContext
    @FetchRequest(
        entity: Certificate.entity(),
        sortDescriptors: [NSSortDescriptor(keyPath: \Certificate.createts, ascending: false)]
        //,predicate: NSPredicate(format: "genre contains 'Action'")
    ) var certificates: FetchedResults<Certificate>
    
    @State var isPresented = false
    
    var body: some View {
        NavigationView {
            List(certificates) { cert in
                NavigationLink( destination: CertificateView(certificate:cert) ) {
                    CertificateRow(certificate:cert)
                }
            }
            .sheet(isPresented: $isPresented) {
                AddCertificate { firstname, lastname, phonenumber, email, type, status, validto in
                    self.addCertificate(
                        firstname: firstname,
                        lastname: lastname,
                        phonenumber: phonenumber,
                        email:email,
                        type:type,
                        status:status,
                        validto:validto
                    )
                    self.isPresented = false
                }
            }
            .navigationTitle("certificatelist_title")
            .navigationBarItems(trailing: addButton())
        }
    }
    
    private func addButton() -> some View {
        return Button(action: { self.isPresented.toggle() }) {Image(systemName: "plus") }
    }
    
    private func deleteCertificate(at offsets: IndexSet) {
        // 1.
        offsets.forEach { index in
            // 2.
            let certificate = self.certificates[index]
            
            // 3.
            self.managedObjectContext.delete(certificate)
        }
        
        // 4.
        saveContext()
    }
    
    
    private func addCertificate(firstname: String, lastname: String, phonenumber: String, email: String, type:CertificateType, status:CertificateStatus, validto:Date) {
        let _ = createCertificate(
            firstName:firstname,
            lastName:lastname,
            phoneNumber:phonenumber,
            email:email,
            validTo: validto,
            status:status,
            type:type,
            context:managedObjectContext
        )
        
        saveContext()
    }
    
    
    private func saveContext() {
        do {
            try managedObjectContext.save()
        } catch {
            print("Error saving managed object context: \(error)")
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
