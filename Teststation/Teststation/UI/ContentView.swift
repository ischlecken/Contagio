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
            List {
              ForEach(certificates) {
                CertificateRow(certificate:$0)
              }
              .onDelete(perform: deleteCertificate)
            }
            .navigationTitle("Testzertifikate")
            .navigationBarItems(trailing: Button("Add") {
                self.addCertificate(firstname: "Stefan", lastname: "Thomas", phonenumber: "0894556655")
            })
        }
    }
    
    func deleteCertificate(at offsets: IndexSet) {
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


    func addCertificate(firstname: String, lastname: String, phonenumber: String) {
        let _ = createCertificate(
            firstName:firstname,
            lastName:lastname,
            phoneNumber:phonenumber,
            context:managedObjectContext
        )

        saveContext()
    }


    func saveContext() {
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
