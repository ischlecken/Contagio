import SwiftUI

struct DetailView: View {
    @State
    var showAlert = false
    
    let msg:String
    var body: some View {
        VStack {
            Text(msg).italic()
            Spacer(minLength: 20)
            Color(.blue)
            Spacer(minLength: 20)
            Button(msg) {
                showAlert = true
            }
            .alert(isPresented: $showAlert) {
                Alert(title:Text("bla"),
                      message: Text("msg is \(msg)"),
                      primaryButton: .destructive(Text("...Delete...")),
                      secondaryButton: .cancel()
                )
            }
        }
    }
}

struct DetailView_Previews: PreviewProvider {
    static var previews: some View {
        DetailView(msg:"bla")
    }
}
