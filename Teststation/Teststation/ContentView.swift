import SwiftUI

struct ContentView: View {
    @Environment(\.managedObjectContext) var managedObjectContext
    @FetchRequest(
      entity: TestCertificate.entity(),
      sortDescriptors: [NSSortDescriptor(keyPath: \TestCertificate.createts, ascending: false)]
      //,predicate: NSPredicate(format: "genre contains 'Action'")
    ) var certificates: FetchedResults<TestCertificate>
    @State var isPresented = false
    
    var body: some View {
        NavigationView {
            List {
              ForEach(certificates, id: \.createts) {
                Text("\($0.firstname!) \($0.lastname!)")
                Text("\($0.phonenumber!)")
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
        let _ = createTestCertificate(
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
        Group {
            ContentView()
        }
    }
}
