import SwiftUI

struct ShowQRView: View {
    
    var body: some View {
        VStack(alignment: .center) {
            Image("passimg")
            Text("scan qr code")
        }
    }
    
}

struct ShowQRView_Previews: PreviewProvider {
    
    static var previews: some View {
        ShowQRView()
    }
}

