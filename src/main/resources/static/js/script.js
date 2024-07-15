console.log("This log is from my javascript file")

const toggleSidebar = () => {
	if($(".sidebar").is(":visible")){
		//closing the side bar
		$(".sidebar").css("display", "none");
		$(".content").css("margin-left", "0%");
	}
	else{
		//opening the side bar
		$(".sidebar").css("display", "block");
		$(".content").css("margin-left", "20%");
	}
};


const search = () => {
	// console.log("Searching...");
	
	let query = $("#search-input").val();

	if (query == "") {
		$(".search-results").hide();
	} else {
		console.log(query);
		//Sending request to server
		let url = `http://localhost:8080/search/${query}`;
		
		fetch(url)
			.then((response) => {
				return response.json();
			})
			.then((data) => {
				//console.log(data);

				let text = `<div class='list-group'>`;

				data.forEach((contact) => {
					text +=`<a href='/user/${contact.cId}/contact' class='list-group-item list-group-item-action'> ${contact.name} </a>`
				});
				text += `</div>`;

				$(".search-results").html(text);
				$(".search-results").show();
			});

		$(".search-results").show();
	}
};