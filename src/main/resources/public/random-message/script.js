// remove any junky replit stuff
document.querySelector("#replit-pill").remove()

async function reveal() {
    const res = await fetch('/random-messages/api')
    const data = await res.json()
    console.log(data)
    document.getElementById("reveledMessage").innerHTML = `<span>${data.msg}</span>`
}

async function createMessage() {
    const message = prompt('Digite uma mensagem')

    if (message) {
        await fetch('/random-messages/api/create', {
            method: "POST",
            headers: {
                "Content-Type": "application/json"
            },
            body: JSON.stringify({
                message
            })
        })
    }
}